package com.example.model

enum class BenchmarkType(val title: String, val subtitle: String, val iconName: String) {
    CPU_ONLY(
        title = "CPU Compute",
        subtitle = "Single-core & multi-core mathematical, hashing, & matrix workloads",
        iconName = "Memory"
    ),
    GPU_ONLY(
        title = "GPU & Frame Timing",
        subtitle = "Real-time particle vortex, dynamic geometry, 1% low frame analysis",
        iconName = "Speed"
    ),
    RAM_ONLY(
        title = "RAM Bandwidth",
        subtitle = "Direct memory streaming read/write & random cache latency",
        iconName = "Layers"
    ),
    DISK_ONLY(
        title = "Storage I/O",
        subtitle = "Sequential read/write throughput and 4K random IOPS",
        iconName = "Storage"
    ),
    TOGETHER_STRESS(
        title = "Combined Stress Test",
        subtitle = "Simultaneous CPU, GPU, RAM, & Disk contention benchmark",
        iconName = "Whatshot"
    )
}

enum class BenchmarkMode(val label: String, val durationSec: Int, val description: String) {
    QUICK(
        label = "Quick Run",
        durationSec = 15,
        description = "Rapid 15s evaluation for quick comparisons"
    ),
    STANDARD(
        label = "Standard Run",
        durationSec = 35,
        description = "Balanced 35s benchmark for accurate points calculation"
    ),
    EXTREME(
        label = "Extreme Stress",
        durationSec = 60,
        description = "Heavy 60s sustained workload testing thermal stability & throttle"
    )
}

enum class LoadPreset(
    val label: String,
    val baseParticleCount: Int,
    val ramBlockMb: Int,
    val diskBlockMb: Int,
    val mathIntensity: Float,
    val description: String
) {
    LIGHT(
        label = "Light",
        baseParticleCount = 1500,
        ramBlockMb = 8,
        diskBlockMb = 8,
        mathIntensity = 0.6f,
        description = "Lower intensity suited for budget hardware & thermal testing"
    ),
    MEDIUM(
        label = "Medium",
        baseParticleCount = 3500,
        ramBlockMb = 16,
        diskBlockMb = 16,
        mathIntensity = 1.0f,
        description = "Standard balanced challenge matching mid-range & high-end devices"
    ),
    HEAVY(
        label = "Heavy",
        baseParticleCount = 7500,
        ramBlockMb = 32,
        diskBlockMb = 24,
        mathIntensity = 1.8f,
        description = "Extreme saturation pushing flagship silicon, bandwidth, & cooling"
    )
}

enum class GpuBackend(val displayName: String, val versionTag: String) {
    VULKAN_1_3("Vulkan 1.3 / 1.4", "Next-Gen Explicit Hardware Pipeline"),
    VULKAN_1_1("Vulkan 1.1 / 1.2", "Standard Low-Overhead Vulkan"),
    OPENGL_ES_3_2("OpenGL ES 3.2", "Core Mobile Modern Pipeline"),
    OPENGL_ES_3_0("OpenGL ES 3.0", "Compatibility Driver Layer"),
    WEBGPU_SKIA("WebGPU / Skia", "Cross-Platform Unified Shader Pipeline")
}

enum class DriverProfile(val displayName: String, val description: String) {
    SYSTEM_DEFAULT("System Default", "Manufacturer OEM Driver (Adreno/Mali/Xclipse)"),
    TURNIP_MESA_ADRENO("Turnip (Mesa/KGSL)", "Open-source Adreno Vulkan driver via user layers"),
    GAME_DRIVER_DEV("Game Driver Hook", "Android 10+ Updatable Graphics Driver Layer"),
    SOFTWARE_SWIFTSHADER("Software (SwiftShader)", "CPU-based reference rasterizer")
}

enum class BenchmarkStatus {
    IDLE,
    RUNNING,
    COMPLETED,
    CANCELLED
}

data class FrameTimingStats(
    val currentFps: Float = 0f,
    val avgFps: Float = 0f,
    val minFps: Float = 0f,
    val maxFps: Float = 0f,
    val low1Percent: Float = 0f,
    val low01Percent: Float = 0f,
    val currentFrameTimeMs: Float = 0f,
    val avgFrameTimeMs: Float = 0f,
    val jankCount: Int = 0,
    val totalFrames: Long = 0L,
    val frameTimeHistory: List<Float> = emptyList(),
    val frameDistribution: Map<String, Int> = mapOf(
        "< 8.3ms" to 0,
        "8.3 - 16.6ms" to 0,
        "16.6 - 33.3ms" to 0,
        "> 33.3ms" to 0
    )
)

data class LiveTelemetry(
    val batteryTempC: Float = 32.5f,
    val ramUsedMb: Long = 3400L,
    val ramTotalMb: Long = 8192L,
    val cpuUsagePercent: Float = 18f,
    val gpuUsagePercent: Float = 45f,
    val privilegedAccess: String = "User Mode" // "User Mode", "Shizuku ADB", "Root Superuser"
)

data class CpuResult(
    val singleCoreScore: Int = 0,
    val multiCoreScore: Int = 0,
    val singleCoreMops: Double = 0.0,
    val multiCoreMops: Double = 0.0,
    val scalingEfficiency: Float = 0f,
    val coreCount: Int = 0,
    val matrixGflops: Double = 0.0,
    val cryptoHashMbps: Double = 0.0,
    val totalCpuScore: Int = 0
)

data class RamResult(
    val readBandwidthGbps: Double = 0.0,
    val writeBandwidthGbps: Double = 0.0,
    val randomThroughputGbps: Double = 0.0,
    val accessLatencyNs: Double = 0.0,
    val ramScore: Int = 0
)

data class DiskResult(
    val seqWriteMbps: Double = 0.0,
    val seqReadMbps: Double = 0.0,
    val randomWriteIops: Int = 0,
    val randomReadIops: Int = 0,
    val diskScore: Int = 0
)

data class GpuResult(
    val avgFps: Float = 0f,
    val low1Fps: Float = 0f,
    val low01Fps: Float = 0f,
    val frameStabilityPercent: Float = 0f,
    val particleCount: Int = 0,
    val totalFramesRendered: Long = 0L,
    val gpuScore: Int = 0
)

data class BenchmarkRun(
    val id: Long = 0L,
    val profileId: Long = 1L,
    val timestamp: Long = System.currentTimeMillis(),
    val type: BenchmarkType,
    val mode: BenchmarkMode,
    val loadPreset: LoadPreset = LoadPreset.MEDIUM,
    val difficultyLevel: Int = 5, // 1 to 10
    val gpuBackend: GpuBackend = GpuBackend.VULKAN_1_3,
    val driverProfile: DriverProfile = DriverProfile.SYSTEM_DEFAULT,
    val overallScore: Int,
    val cpuResult: CpuResult? = null,
    val ramResult: RamResult? = null,
    val diskResult: DiskResult? = null,
    val gpuResult: GpuResult? = null,
    val deviceModel: String = "",
    val deviceManufacturer: String = "",
    val osVersion: String = "",
    val durationSec: Int = 0,
    val thermalThrottlingDetected: Boolean = false,
    val stabilityRating: String = "Stable"
)

data class UserProfile(
    val id: Long = 1L,
    val username: String = "default_user",
    val displayName: String = "Benchmarking Master",
    val deviceAlias: String = "Primary Device",
    val avatarColorHex: String = "#00E5FF",
    val createdAt: Long = System.currentTimeMillis()
)

data class HardwareTier(
    val id: String,
    val name: String,
    val category: String,
    val chipset: String,
    val overallScore: Int,
    val cpuScore: Int,
    val gpuScore: Int,
    val ramScore: Int,
    val diskScore: Int,
    val tierBadge: String,
    val description: String
)

data class HardwareProfile(
    val model: String,
    val manufacturer: String,
    val deviceName: String,
    val androidVersion: String,
    val apiLevel: Int,
    val cpuAbi: String,
    val availableProcessors: Int,
    val totalRamBytes: Long,
    val availRamBytes: Long,
    val totalStorageBytes: Long,
    val freeStorageBytes: Long,
    val displayResolution: String,
    val refreshRateHz: Float
)
