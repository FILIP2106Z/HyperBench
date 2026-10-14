package com.example

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.BenchmarkViewModel
import com.example.ui.screens.ActiveBenchmarkScreen
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.GpuDriverScreen
import com.example.ui.screens.HardwareInfoScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MultiPlatformDocsScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MyApplicationTheme {
                val viewModel = remember { BenchmarkViewModel(applicationContext) }
                BenchmarkApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BenchmarkApp(viewModel: BenchmarkViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val historyRuns by viewModel.historyRuns.collectAsState()
    val userProfiles by viewModel.userProfiles.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val globalRankings by viewModel.globalRankings.collectAsState()

    val context = LocalContext.current
    val showBottomBar = uiState.currentScreen != AppScreen.ACTIVE_BENCHMARK

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val currentScreen = uiState.currentScreen
                    val isHome = currentScreen == AppScreen.HOME || currentScreen == AppScreen.RESULTS

                    NavigationBarItem(
                        selected = isHome,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Benchmark") },
                        label = { Text("Bench", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_benchmark")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.LEADERBOARD,
                        onClick = { viewModel.navigateTo(AppScreen.LEADERBOARD) },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Rankings") },
                        label = { Text("Rankings", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_rankings")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.COMPARE,
                        onClick = { viewModel.navigateTo(AppScreen.COMPARE) },
                        icon = { Icon(Icons.Default.CompareArrows, contentDescription = "Compare") },
                        label = { Text("Compare", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_compare")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HISTORY,
                        onClick = { viewModel.navigateTo(AppScreen.HISTORY) },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_history")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HARDWARE_SPECS,
                        onClick = { viewModel.navigateTo(AppScreen.HARDWARE_SPECS) },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Specs") },
                        label = { Text("Specs", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_specs")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    uiState = uiState,
                    onSelectType = { viewModel.selectBenchmarkType(it) },
                    onSelectMode = { viewModel.selectBenchmarkMode(it) },
                    onSelectPreset = { viewModel.selectLoadPreset(it) },
                    onSetDifficulty = { viewModel.setDifficultyLevel(it) },
                    onAutoBalance = { viewModel.autoBalanceToHardware() },
                    onSetParticleCount = { viewModel.setGpuParticleCount(it) },
                    onToggleComplexMesh = { viewModel.toggleMeshComplexity(it) },
                    onStartBenchmark = { viewModel.startBenchmark() },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                AppScreen.LEADERBOARD -> LeaderboardScreen(
                    currentUser = currentUser,
                    isAuthLoading = isAuthLoading,
                    authError = authError,
                    rankings = globalRankings,
                    latestRun = uiState.currentRun ?: uiState.latestCompletedRun,
                    isUploading = uiState.isUploadingRanking,
                    onSignInWithGoogle = { activity -> viewModel.signInWithGoogle(activity) },
                    onSignOutGoogle = { viewModel.signOutGoogle() },
                    onSubmitRun = { run ->
                        viewModel.submitRunToLeaderboard(run) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                AppScreen.ACTIVE_BENCHMARK -> ActiveBenchmarkScreen(
                    uiState = uiState,
                    gpuEngine = viewModel.controller.gpuEngine,
                    onFrameStats = { viewModel.updateFrameStats(it) },
                    onCancel = { viewModel.navigateTo(AppScreen.HOME) }
                )

                AppScreen.RESULTS -> ResultsScreen(
                    run = uiState.currentRun ?: uiState.latestCompletedRun,
                    exportJsonString = uiState.exportJsonString,
                    onNavigate = { viewModel.navigateTo(it) },
                    onRunAgain = { viewModel.startBenchmark() }
                )

                AppScreen.COMPARE -> CompareScreen(
                    currentRun = uiState.currentRun ?: uiState.latestCompletedRun,
                    selectedTier = uiState.selectedCompareTier,
                    onSelectTier = { viewModel.selectCompareTier(it) },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                AppScreen.HISTORY -> HistoryScreen(
                    runs = historyRuns,
                    onInspectRun = { viewModel.inspectRun(it) },
                    onDeleteRun = { viewModel.deleteRun(it) },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                AppScreen.HARDWARE_SPECS -> HardwareInfoScreen(
                    profile = uiState.hardwareProfile,
                    onNavigate = { viewModel.navigateTo(it) }
                )

                AppScreen.USER_PROFILE -> UserProfileScreen(
                    currentProfile = uiState.activeProfile,
                    allProfiles = userProfiles,
                    userRuns = historyRuns,
                    onSwitchProfile = { viewModel.switchProfile(it) },
                    onCreateProfile = { username, displayName, deviceAlias, colorHex ->
                        viewModel.createProfile(username, displayName, deviceAlias, colorHex)
                    },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                AppScreen.GPU_DRIVER_SETTINGS -> GpuDriverScreen(
                    currentBackend = uiState.selectedGpuBackend,
                    currentDriver = uiState.selectedDriverProfile,
                    liveTelemetry = uiState.liveTelemetry,
                    privilegedDetails = uiState.privilegedDetails,
                    privilegedActionMessage = uiState.privilegedActionMessage,
                    onSelectBackend = { viewModel.setGpuBackend(it) },
                    onSelectDriver = { viewModel.setDriverProfile(it) },
                    onRequestRoot = {
                        viewModel.requestRootAccess { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenShizuku = {
                        viewModel.openShizukuApp { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRefreshPrivileged = { viewModel.refreshPrivilegedStatus() },
                    onTogglePrivilegedOverride = { viewModel.setPrivilegedOverride(it) },
                    onNavigate = { viewModel.navigateTo(it) }
                )

                AppScreen.MULTI_PLATFORM_DOCS -> MultiPlatformDocsScreen(
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    }
}
