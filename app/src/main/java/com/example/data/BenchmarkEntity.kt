package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
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

@Entity(tableName = "benchmark_runs")
data class BenchmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val profileId: Long = 1L,
    val timestamp: Long,
    val typeName: String,
    val modeName: String,
    val presetName: String = "MEDIUM",
    val difficultyLevel: Int = 5,
    val gpuBackendName: String = "VULKAN_1_3",
    val driverProfileName: String = "SYSTEM_DEFAULT",
    val overallScore: Int,
    val singleCoreScore: Int = 0,
    val multiCoreScore: Int = 0,
    val singleCoreMops: Double = 0.0,
    val multiCoreMops: Double = 0.0,
    val coreCount: Int = 0,
    val ramReadGbps: Double = 0.0,
    val ramWriteGbps: Double = 0.0,
    val ramLatencyNs: Double = 0.0,
    val ramScore: Int = 0,
    val diskSeqWriteMbps: Double = 0.0,
    val diskSeqReadMbps: Double = 0.0,
    val diskRandomWriteIops: Int = 0,
    val diskRandomReadIops: Int = 0,
    val diskScore: Int = 0,
    val gpuAvgFps: Float = 0f,
    val gpuLow1Fps: Float = 0f,
    val gpuLow01Fps: Float = 0f,
    val gpuStabilityPercent: Float = 0f,
    val gpuParticleCount: Int = 0,
    val gpuScore: Int = 0,
    val deviceModel: String,
    val deviceManufacturer: String,
    val osVersion: String,
    val durationSec: Int,
    val thermalThrottling: Boolean,
    val stabilityRating: String
) {
    fun toBenchmarkRun(): BenchmarkRun {
        val type = try { BenchmarkType.valueOf(typeName) } catch (_: Exception) { BenchmarkType.TOGETHER_STRESS }
        val mode = try { BenchmarkMode.valueOf(modeName) } catch (_: Exception) { BenchmarkMode.STANDARD }
        val preset = try { LoadPreset.valueOf(presetName) } catch (_: Exception) { LoadPreset.MEDIUM }
        val backend = try { GpuBackend.valueOf(gpuBackendName) } catch (_: Exception) { GpuBackend.VULKAN_1_3 }
        val driver = try { DriverProfile.valueOf(driverProfileName) } catch (_: Exception) { DriverProfile.SYSTEM_DEFAULT }

        val cpu = if (singleCoreScore > 0 || multiCoreScore > 0) {
            CpuResult(
                singleCoreScore = singleCoreScore,
                multiCoreScore = multiCoreScore,
                singleCoreMops = singleCoreMops,
                multiCoreMops = multiCoreMops,
                coreCount = coreCount,
                totalCpuScore = if (type == BenchmarkType.CPU_ONLY) overallScore else ((singleCoreScore * 0.35) + (multiCoreScore * 0.65)).toInt()
            )
        } else null

        val ram = if (ramScore > 0 || ramReadGbps > 0.0) {
            RamResult(
                readBandwidthGbps = ramReadGbps,
                writeBandwidthGbps = ramWriteGbps,
                accessLatencyNs = ramLatencyNs,
                ramScore = ramScore
            )
        } else null

        val disk = if (diskScore > 0 || diskSeqReadMbps > 0.0) {
            DiskResult(
                seqWriteMbps = diskSeqWriteMbps,
                seqReadMbps = diskSeqReadMbps,
                randomWriteIops = diskRandomWriteIops,
                randomReadIops = diskRandomReadIops,
                diskScore = diskScore
            )
        } else null

        val gpu = if (gpuScore > 0 || gpuAvgFps > 0f) {
            GpuResult(
                avgFps = gpuAvgFps,
                low1Fps = gpuLow1Fps,
                low01Fps = gpuLow01Fps,
                frameStabilityPercent = gpuStabilityPercent,
                particleCount = gpuParticleCount,
                gpuScore = gpuScore
            )
        } else null

        return BenchmarkRun(
            id = id,
            profileId = profileId,
            timestamp = timestamp,
            type = type,
            mode = mode,
            loadPreset = preset,
            difficultyLevel = difficultyLevel,
            gpuBackend = backend,
            driverProfile = driver,
            overallScore = overallScore,
            cpuResult = cpu,
            ramResult = ram,
            diskResult = disk,
            gpuResult = gpu,
            deviceModel = deviceModel,
            deviceManufacturer = deviceManufacturer,
            osVersion = osVersion,
            durationSec = durationSec,
            thermalThrottlingDetected = thermalThrottling,
            stabilityRating = stabilityRating
        )
    }

    companion object {
        fun fromBenchmarkRun(run: BenchmarkRun): BenchmarkEntity {
            return BenchmarkEntity(
                id = run.id,
                profileId = run.profileId,
                timestamp = run.timestamp,
                typeName = run.type.name,
                modeName = run.mode.name,
                presetName = run.loadPreset.name,
                difficultyLevel = run.difficultyLevel,
                gpuBackendName = run.gpuBackend.name,
                driverProfileName = run.driverProfile.name,
                overallScore = run.overallScore,
                singleCoreScore = run.cpuResult?.singleCoreScore ?: 0,
                multiCoreScore = run.cpuResult?.multiCoreScore ?: 0,
                singleCoreMops = run.cpuResult?.singleCoreMops ?: 0.0,
                multiCoreMops = run.cpuResult?.multiCoreMops ?: 0.0,
                coreCount = run.cpuResult?.coreCount ?: 0,
                ramReadGbps = run.ramResult?.readBandwidthGbps ?: 0.0,
                ramWriteGbps = run.ramResult?.writeBandwidthGbps ?: 0.0,
                ramLatencyNs = run.ramResult?.accessLatencyNs ?: 0.0,
                ramScore = run.ramResult?.ramScore ?: 0,
                diskSeqWriteMbps = run.diskResult?.seqWriteMbps ?: 0.0,
                diskSeqReadMbps = run.diskResult?.seqReadMbps ?: 0.0,
                diskRandomWriteIops = run.diskResult?.randomWriteIops ?: 0,
                diskRandomReadIops = run.diskResult?.randomReadIops ?: 0,
                diskScore = run.diskResult?.diskScore ?: 0,
                gpuAvgFps = run.gpuResult?.avgFps ?: 0f,
                gpuLow1Fps = run.gpuResult?.low1Fps ?: 0f,
                gpuLow01Fps = run.gpuResult?.low01Fps ?: 0f,
                gpuStabilityPercent = run.gpuResult?.frameStabilityPercent ?: 0f,
                gpuParticleCount = run.gpuResult?.particleCount ?: 0,
                gpuScore = run.gpuResult?.gpuScore ?: 0,
                deviceModel = run.deviceModel,
                deviceManufacturer = run.deviceManufacturer,
                osVersion = run.osVersion,
                durationSec = run.durationSec,
                thermalThrottling = run.thermalThrottlingDetected,
                stabilityRating = run.stabilityRating
            )
        }
    }
}
