package com.example.benchmark

import android.content.Context
import android.os.Build
import com.example.model.BenchmarkMode
import com.example.model.BenchmarkRun
import com.example.model.BenchmarkType
import com.example.model.CpuResult
import com.example.model.DiskResult
import com.example.model.DriverProfile
import com.example.model.GpuBackend
import com.example.model.GpuResult
import com.example.model.LoadPreset
import com.example.model.RamResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class BenchmarkController(private val context: Context) {

    val cpuEngine = CpuBenchmark()
    val ramEngine = RamBenchmark()
    val diskEngine = DiskBenchmark(context)
    val gpuEngine = GpuBenchmarkEngine()

    suspend fun executeBenchmark(
        type: BenchmarkType,
        mode: BenchmarkMode,
        preset: LoadPreset = LoadPreset.MEDIUM,
        difficultyLevel: Int = 5,
        gpuBackend: GpuBackend = GpuBackend.VULKAN_1_3,
        driverProfile: DriverProfile = DriverProfile.SYSTEM_DEFAULT,
        profileId: Long = 1L,
        onStageUpdate: (progress: Float, stage: String) -> Unit,
        onLiveStats: (currentStat: String) -> Unit
    ): BenchmarkRun = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        val duration = mode.durationSec
        val difficultyMultiplier = (difficultyLevel / 5.0f).coerceIn(0.2f, 2.0f)

        var cpuResult: CpuResult? = null
        var ramResult: RamResult? = null
        var diskResult: DiskResult? = null
        var gpuResult: GpuResult? = null
        var thermalThrottling = false

        when (type) {
            BenchmarkType.CPU_ONLY -> {
                cpuResult = cpuEngine.runBenchmark(durationSec = duration) { prog, msg ->
                    onStageUpdate(prog, msg)
                    onLiveStats(msg)
                }
            }

            BenchmarkType.RAM_ONLY -> {
                ramResult = ramEngine.runBenchmark(durationSec = duration) { prog, msg ->
                    onStageUpdate(prog, msg)
                    onLiveStats(msg)
                }
            }

            BenchmarkType.DISK_ONLY -> {
                diskResult = diskEngine.runBenchmark(durationSec = duration) { prog, msg ->
                    onStageUpdate(prog, msg)
                    onLiveStats(msg)
                }
            }

            BenchmarkType.GPU_ONLY -> {
                val steps = duration * 4
                for (i in 0..steps) {
                    delay(250)
                    val prog = i.toFloat() / steps
                    onStageUpdate(prog, "Rendering with ${gpuBackend.displayName} (${(prog * 100).toInt()}%)")
                }
                gpuResult = gpuEngine.finalizeResult()
            }

            BenchmarkType.TOGETHER_STRESS -> {
                onStageUpdate(0.05f, "Initializing Full System Contention Benchmark (${preset.label} Preset)...")

                coroutineScope {
                    val cpuJob = async(Dispatchers.Default) {
                        cpuEngine.runBenchmark(durationSec = duration) { _, m ->
                            onLiveStats("CPU Load: $m")
                        }
                    }

                    val ramJob = async(Dispatchers.Default) {
                        ramEngine.runBenchmark(durationSec = duration) { _, m ->
                            onLiveStats("RAM Bus: $m")
                        }
                    }

                    val diskJob = async(Dispatchers.IO) {
                        diskEngine.runBenchmark(durationSec = duration) { _, m ->
                            onLiveStats("Disk I/O: $m")
                        }
                    }

                    val ticks = duration * 2
                    for (t in 0..ticks) {
                        delay(500)
                        val p = t.toFloat() / ticks
                        onStageUpdate(p, "Simultaneous Multi-Subsystem Contention (${(p * 100).toInt()}%)")
                    }

                    cpuResult = cpuJob.await()
                    ramResult = ramJob.await()
                    diskResult = diskJob.await()
                    gpuResult = gpuEngine.finalizeResult()
                }

                if ((gpuResult?.frameStabilityPercent ?: 100f) < 70f || (cpuResult?.scalingEfficiency ?: 1f) < 0.55f) {
                    thermalThrottling = true
                }
            }
        }

        // Apply difficulty scaling factor to score calculation
        val rawOverall = calculateOverallScore(type, cpuResult, gpuResult, ramResult, diskResult)
        val overallScore = (rawOverall * (0.85f + (difficultyMultiplier * 0.15f))).toInt()

        val stability = when {
            thermalThrottling -> "Throttled (High Contention)"
            (gpuResult?.frameStabilityPercent ?: 100f) > 90f -> "Optimal (Fluid)"
            else -> "Normal"
        }

        BenchmarkRun(
            id = System.currentTimeMillis(),
            profileId = profileId,
            timestamp = startTime,
            type = type,
            mode = mode,
            loadPreset = preset,
            difficultyLevel = difficultyLevel,
            gpuBackend = gpuBackend,
            driverProfile = driverProfile,
            overallScore = overallScore,
            cpuResult = cpuResult,
            ramResult = ramResult,
            diskResult = diskResult,
            gpuResult = gpuResult,
            deviceModel = Build.MODEL,
            deviceManufacturer = Build.MANUFACTURER,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            durationSec = duration,
            thermalThrottlingDetected = thermalThrottling,
            stabilityRating = stability
        )
    }

    private fun calculateOverallScore(
        type: BenchmarkType,
        cpu: CpuResult?,
        gpu: GpuResult?,
        ram: RamResult?,
        disk: DiskResult?
    ): Int {
        return when (type) {
            BenchmarkType.CPU_ONLY -> cpu?.totalCpuScore ?: 0
            BenchmarkType.GPU_ONLY -> gpu?.gpuScore ?: 0
            BenchmarkType.RAM_ONLY -> ram?.ramScore ?: 0
            BenchmarkType.DISK_ONLY -> disk?.diskScore ?: 0
            BenchmarkType.TOGETHER_STRESS -> {
                val c = cpu?.totalCpuScore ?: 2000
                val g = gpu?.gpuScore ?: 2500
                val r = ram?.ramScore ?: 2000
                val d = disk?.diskScore ?: 1800
                ((c * 0.32) + (g * 0.35) + (r * 0.18) + (d * 0.15)).toInt()
            }
        }
    }
}
