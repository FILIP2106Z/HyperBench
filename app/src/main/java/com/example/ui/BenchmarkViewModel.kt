package com.example.ui

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.Display
import android.view.WindowManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.benchmark.BenchmarkController
import com.example.benchmark.PrivilegedTelemetryManager
import com.example.data.BenchmarkDatabase
import com.example.data.BenchmarkEntity
import com.example.data.BenchmarkExportManager
import com.example.data.PresetHardwareDatabase
import com.example.data.UserProfileEntity
import com.example.model.BenchmarkMode
import com.example.model.BenchmarkRun
import com.example.model.BenchmarkStatus
import com.example.model.BenchmarkType
import com.example.model.DriverProfile
import com.example.model.FrameTimingStats
import com.example.model.GpuBackend
import com.example.model.HardwareProfile
import com.example.model.HardwareTier
import com.example.model.LiveTelemetry
import com.example.model.LoadPreset
import com.example.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    ACTIVE_BENCHMARK,
    RESULTS,
    COMPARE,
    HISTORY,
    HARDWARE_SPECS,
    USER_PROFILE,
    GPU_DRIVER_SETTINGS,
    MULTI_PLATFORM_DOCS,
    LEADERBOARD
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val selectedType: BenchmarkType = BenchmarkType.TOGETHER_STRESS,
    val selectedMode: BenchmarkMode = BenchmarkMode.STANDARD,
    val selectedLoadPreset: LoadPreset = LoadPreset.MEDIUM,
    val difficultyLevel: Int = 5,
    val isAutoHardwareBalanced: Boolean = true,
    val selectedGpuBackend: GpuBackend = GpuBackend.VULKAN_1_3,
    val selectedDriverProfile: DriverProfile = DriverProfile.SYSTEM_DEFAULT,
    val benchmarkStatus: BenchmarkStatus = BenchmarkStatus.IDLE,
    val progress: Float = 0f,
    val currentStage: String = "",
    val liveStatsText: String = "",
    val activeFrameStats: FrameTimingStats = FrameTimingStats(),
    val liveTelemetry: LiveTelemetry = LiveTelemetry(),
    val currentRun: BenchmarkRun? = null,
    val latestCompletedRun: BenchmarkRun? = null,
    val activeProfile: UserProfile = UserProfile(),
    val selectedCompareTier: HardwareTier = PresetHardwareDatabase.hardwareTiers[1],
    val gpuParticleCount: Int = 3500,
    val gpuIsComplexMesh: Boolean = true,
    val exportJsonString: String? = null,
    val hardwareProfile: HardwareProfile? = null,
    val isUploadingRanking: Boolean = false,
    val rankingUploadStatus: String? = null,
    val privilegedDetails: Map<String, String> = emptyMap(),
    val privilegedActionMessage: String? = null
)

class BenchmarkViewModel(private val context: Context) : ViewModel() {

    private val db = BenchmarkDatabase.getInstance(context)
    private val dao = db.benchmarkDao()
    val controller = BenchmarkController(context)
    val telemetryManager = PrivilegedTelemetryManager(context)
    val authManager = com.example.auth.CloudAuthManager(context)
    val rankingRepo = com.example.data.RankingRepository(context)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val currentUser = authManager.currentUser
    val authError = authManager.authError
    val isAuthLoading = authManager.isLoading

    val globalRankings = rankingRepo.observeGlobalLeaderboard()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val historyRuns: StateFlow<List<BenchmarkRun>> = dao.getAllRuns()
        .map { entities -> entities.map { it.toBenchmarkRun() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userProfiles: StateFlow<List<UserProfile>> = dao.getAllProfiles()
        .map { list ->
            if (list.isEmpty()) {
                listOf(UserProfile())
            } else {
                list.map { it.toUserProfile() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf(UserProfile()))

    init {
        loadHardwareSpecsSafely()
        initDefaultProfile()
        startTelemetryLoop()
        authManager.attemptSilentSignIn(viewModelScope)
    }

    private fun initDefaultProfile() {
        viewModelScope.launch {
            try {
                val profiles = dao.getAllProfiles()
                // Ensure at least one default profile exists
                val defaultEntity = UserProfileEntity(
                    id = 1L,
                    username = "primary_tester",
                    displayName = "Master Overclocker",
                    deviceAlias = "${Build.MANUFACTURER} ${Build.MODEL}",
                    avatarColorHex = "#00E5FF",
                    isCurrent = true
                )
                dao.insertProfile(defaultEntity)
                _uiState.value = _uiState.value.copy(activeProfile = defaultEntity.toUserProfile())
            } catch (_: Exception) {}
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val fps = _uiState.value.activeFrameStats.currentFps.takeIf { it > 0f } ?: 60f
                    val telemetry = telemetryManager.readLiveTelemetry(fps)
                    _uiState.value = _uiState.value.copy(liveTelemetry = telemetry)
                } catch (_: Exception) {}
                delay(1200)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun selectBenchmarkType(type: BenchmarkType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun selectBenchmarkMode(mode: BenchmarkMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }

    fun selectLoadPreset(preset: LoadPreset) {
        val difficulty = when (preset) {
            LoadPreset.LIGHT -> 3
            LoadPreset.MEDIUM -> 5
            LoadPreset.HEAVY -> 8
        }
        val particleCount = (preset.baseParticleCount * (difficulty / 5f)).toInt()
        _uiState.value = _uiState.value.copy(
            selectedLoadPreset = preset,
            difficultyLevel = difficulty,
            isAutoHardwareBalanced = false,
            gpuParticleCount = particleCount
        )
        controller.gpuEngine.currentParticleCount = particleCount
    }

    fun setDifficultyLevel(level: Int) {
        val clamped = level.coerceIn(1, 10)
        val base = _uiState.value.selectedLoadPreset.baseParticleCount
        val particleCount = (base * (clamped / 5f)).toInt().coerceIn(1000, 10000)
        _uiState.value = _uiState.value.copy(
            difficultyLevel = clamped,
            isAutoHardwareBalanced = false,
            gpuParticleCount = particleCount
        )
        controller.gpuEngine.currentParticleCount = particleCount
    }

    fun autoBalanceToHardware() {
        val profile = _uiState.value.hardwareProfile
        val ramGb = profile?.let { it.totalRamBytes / (1024L * 1024L * 1024L) } ?: 8L
        val cores = profile?.availableProcessors ?: 8

        val (preset, difficulty) = when {
            ramGb >= 12 && cores >= 8 -> LoadPreset.HEAVY to 8
            ramGb >= 6 && cores >= 6 -> LoadPreset.MEDIUM to 5
            else -> LoadPreset.LIGHT to 3
        }
        val particleCount = (preset.baseParticleCount * (difficulty / 5f)).toInt()
        _uiState.value = _uiState.value.copy(
            selectedLoadPreset = preset,
            difficultyLevel = difficulty,
            isAutoHardwareBalanced = true,
            gpuParticleCount = particleCount
        )
        controller.gpuEngine.currentParticleCount = particleCount
    }

    fun setGpuBackend(backend: GpuBackend) {
        _uiState.value = _uiState.value.copy(selectedGpuBackend = backend)
    }

    fun setDriverProfile(driver: DriverProfile) {
        _uiState.value = _uiState.value.copy(selectedDriverProfile = driver)
    }

    fun setPrivilegedOverride(mode: String?) {
        telemetryManager.simulatedElevatedMode = mode
        refreshPrivilegedStatus()
    }

    fun requestRootAccess(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = telemetryManager.requestRootPermission()
            if (res.isSuccess) {
                val tele = telemetryManager.readLiveTelemetry()
                val metrics = telemetryManager.readPrivilegedHardwareMetrics()
                _uiState.value = _uiState.value.copy(
                    liveTelemetry = tele,
                    privilegedDetails = metrics,
                    privilegedActionMessage = res.getOrNull()
                )
                onResult(true, res.getOrNull() ?: "Root Superuser Active")
            } else {
                val err = res.exceptionOrNull()?.message ?: "Root request failed"
                _uiState.value = _uiState.value.copy(privilegedActionMessage = err)
                onResult(false, err)
            }
        }
    }

    fun openShizukuApp(onResult: (Boolean, String) -> Unit) {
        val intent = telemetryManager.getShizukuLaunchIntent()
        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
                onResult(true, "Opening Shizuku Manager. Return to HyperBench after starting Shizuku service.")
            } catch (e: Exception) {
                onResult(false, "Could not open Shizuku: ${e.message}")
            }
        } else {
            onResult(false, "Shizuku app is not installed (moe.shizuku.privileged.api). Please install Shizuku from GitHub or Play Store.")
        }
    }

    fun refreshPrivilegedStatus() {
        viewModelScope.launch {
            val tele = telemetryManager.readLiveTelemetry()
            val metrics = telemetryManager.readPrivilegedHardwareMetrics()
            _uiState.value = _uiState.value.copy(liveTelemetry = tele, privilegedDetails = metrics)
        }
    }

    fun setGpuParticleCount(count: Int) {
        _uiState.value = _uiState.value.copy(gpuParticleCount = count)
        controller.gpuEngine.currentParticleCount = count
    }

    fun toggleMeshComplexity(complex: Boolean) {
        _uiState.value = _uiState.value.copy(gpuIsComplexMesh = complex)
        controller.gpuEngine.isGeometryComplex = complex
    }

    fun selectCompareTier(tier: HardwareTier) {
        _uiState.value = _uiState.value.copy(selectedCompareTier = tier)
    }

    fun updateFrameStats(stats: FrameTimingStats) {
        if (_uiState.value.benchmarkStatus == BenchmarkStatus.RUNNING) {
            _uiState.value = _uiState.value.copy(activeFrameStats = stats)
        }
    }

    fun signInWithGoogle(activity: android.app.Activity) {
        authManager.signInWithGoogle(activity, viewModelScope)
    }

    fun signOutGoogle() {
        authManager.signOut(viewModelScope)
    }

    fun submitRunToLeaderboard(run: BenchmarkRun, onComplete: (Boolean, String) -> Unit) {
        val user = authManager.currentUser.value
        if (user == null) {
            onComplete(false, "Please sign in with Google to publish score to global leaderboard")
            return
        }
        _uiState.value = _uiState.value.copy(isUploadingRanking = true, rankingUploadStatus = "Uploading score to global cloud...")
        viewModelScope.launch {
            val res = rankingRepo.submitRunToLeaderboard(run, user)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(isUploadingRanking = false, rankingUploadStatus = "Score successfully ranked!")
                onComplete(true, "Your score of ${run.overallScore} pts was published to the global rankings!")
            } else {
                val err = res.exceptionOrNull()?.localizedMessage ?: "Failed to upload score"
                _uiState.value = _uiState.value.copy(isUploadingRanking = false, rankingUploadStatus = "Upload failed: $err")
                onComplete(false, err)
            }
        }
    }

    // Profile Management
    fun createProfile(username: String, displayName: String, deviceAlias: String, colorHex: String) {
        viewModelScope.launch {
            val entity = UserProfileEntity(
                username = username.ifBlank { "user_${System.currentTimeMillis() % 1000}" },
                displayName = displayName.ifBlank { "Benchmark Tester" },
                deviceAlias = deviceAlias.ifBlank { "Mobile Rig" },
                avatarColorHex = colorHex,
                isCurrent = true
            )
            val id = dao.insertProfile(entity)
            val profile = entity.copy(id = id).toUserProfile()
            _uiState.value = _uiState.value.copy(activeProfile = profile)
        }
    }

    fun switchProfile(profile: UserProfile) {
        _uiState.value = _uiState.value.copy(activeProfile = profile)
    }

    fun startBenchmark() {
        val currentType = _uiState.value.selectedType
        val currentMode = _uiState.value.selectedMode
        val currentPreset = _uiState.value.selectedLoadPreset
        val currentDiff = _uiState.value.difficultyLevel
        val currentBackend = _uiState.value.selectedGpuBackend
        val currentDriver = _uiState.value.selectedDriverProfile
        val profileId = _uiState.value.activeProfile.id

        controller.gpuEngine.reset(particleCount = _uiState.value.gpuParticleCount)

        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.ACTIVE_BENCHMARK,
            benchmarkStatus = BenchmarkStatus.RUNNING,
            progress = 0f,
            currentStage = "Starting ${currentType.title} (${currentPreset.label} Load)...",
            liveStatsText = "Calibrating timers...",
            activeFrameStats = FrameTimingStats()
        )

        viewModelScope.launch {
            try {
                val run = controller.executeBenchmark(
                    type = currentType,
                    mode = currentMode,
                    preset = currentPreset,
                    difficultyLevel = currentDiff,
                    gpuBackend = currentBackend,
                    driverProfile = currentDriver,
                    profileId = profileId,
                    onStageUpdate = { prog, stage ->
                        _uiState.value = _uiState.value.copy(
                            progress = prog,
                            currentStage = stage
                        )
                    },
                    onLiveStats = { statText ->
                        _uiState.value = _uiState.value.copy(liveStatsText = statText)
                    }
                )

                val entity = BenchmarkEntity.fromBenchmarkRun(run)
                val id = dao.insertRun(entity)
                val savedRun = run.copy(id = id)

                _uiState.value = _uiState.value.copy(
                    benchmarkStatus = BenchmarkStatus.COMPLETED,
                    currentScreen = AppScreen.RESULTS,
                    currentRun = savedRun,
                    latestCompletedRun = savedRun,
                    exportJsonString = BenchmarkExportManager.exportToJson(savedRun)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    benchmarkStatus = BenchmarkStatus.IDLE,
                    currentStage = "Benchmark cancelled or failed: ${e.message}"
                )
            }
        }
    }

    fun inspectRun(run: BenchmarkRun) {
        _uiState.value = _uiState.value.copy(
            currentRun = run,
            currentScreen = AppScreen.RESULTS,
            exportJsonString = BenchmarkExportManager.exportToJson(run)
        )
    }

    fun deleteRun(run: BenchmarkRun) {
        viewModelScope.launch {
            val entity = BenchmarkEntity.fromBenchmarkRun(run)
            dao.deleteRun(entity)
            if (_uiState.value.currentRun?.id == run.id) {
                _uiState.value = _uiState.value.copy(currentRun = null, currentScreen = AppScreen.HISTORY)
            }
        }
    }

    /**
     * Completely safe hardware inspection that NEVER throws on Android API 30+, emulators, or VMs.
     */
    private fun loadHardwareSpecsSafely() {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)

            var totalStorage = 64L * 1024L * 1024L * 1024L
            var availStorage = 32L * 1024L * 1024L * 1024L
            try {
                val dataPath = Environment.getDataDirectory()?.path
                if (dataPath != null) {
                    val statFs = StatFs(dataPath)
                    val blockSize = statFs.blockSizeLong
                    totalStorage = statFs.blockCountLong * blockSize
                    availStorage = statFs.availableBlocksLong * blockSize
                }
            } catch (_: Exception) {}

            var refreshRate = 60f
            var res = "1080 x 2400"
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                    val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)
                    if (display != null) {
                        refreshRate = display.refreshRate
                        val mode = display.mode
                        if (mode != null) {
                            res = "${mode.physicalWidth} x ${mode.physicalHeight}"
                        }
                    }
                } else {
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                    @Suppress("DEPRECATION")
                    val display = wm?.defaultDisplay
                    if (display != null) {
                        refreshRate = display.refreshRate
                    }
                }
            } catch (_: Exception) {}

            val profile = HardwareProfile(
                model = Build.MODEL ?: "Virtual Device",
                manufacturer = Build.MANUFACTURER ?: "Android",
                deviceName = Build.DEVICE ?: "Generic",
                androidVersion = Build.VERSION.RELEASE ?: "14",
                apiLevel = Build.VERSION.SDK_INT,
                cpuAbi = Build.SUPPORTED_ABIS?.firstOrNull() ?: "arm64-v8a",
                availableProcessors = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
                totalRamBytes = if (memInfo.totalMem > 0) memInfo.totalMem else 8L * 1024L * 1024L * 1024L,
                availRamBytes = if (memInfo.availMem > 0) memInfo.availMem else 4L * 1024L * 1024L * 1024L,
                totalStorageBytes = totalStorage,
                freeStorageBytes = availStorage,
                displayResolution = res,
                refreshRateHz = refreshRate
            )
            _uiState.value = _uiState.value.copy(hardwareProfile = profile)
            autoBalanceToHardware()
        } catch (_: Exception) {
            // Default safe profile if any system API fails
            val fallback = HardwareProfile(
                model = "Android Device",
                manufacturer = "Generic",
                deviceName = "generic_arm64",
                androidVersion = "14",
                apiLevel = 34,
                cpuAbi = "arm64-v8a",
                availableProcessors = 8,
                totalRamBytes = 8L * 1024L * 1024L * 1024L,
                availRamBytes = 4L * 1024L * 1024L * 1024L,
                totalStorageBytes = 64L * 1024L * 1024L * 1024L,
                freeStorageBytes = 32L * 1024L * 1024L * 1024L,
                displayResolution = "1080 x 2400",
                refreshRateHz = 60f
            )
            _uiState.value = _uiState.value.copy(hardwareProfile = fallback)
        }
    }
}
