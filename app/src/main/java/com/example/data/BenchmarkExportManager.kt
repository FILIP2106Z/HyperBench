package com.example.data

import com.example.model.BenchmarkRun
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BenchmarkExportManager {

    private const val SCHEMA_VERSION = "2.1.0"

    fun exportToJson(run: BenchmarkRun): String {
        val json = JSONObject()
        json.put("schemaVersion", SCHEMA_VERSION)
        json.put("app", "HyperBench")
        json.put("id", run.id)
        json.put("timestamp", run.timestamp)
        json.put("dateFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(run.timestamp)))
        json.put("deviceModel", run.deviceModel)
        json.put("deviceManufacturer", run.deviceManufacturer)
        json.put("osVersion", run.osVersion)
        json.put("benchmarkType", run.type.name)
        json.put("benchmarkMode", run.mode.name)
        json.put("durationSec", run.durationSec)
        json.put("overallScore", run.overallScore)
        json.put("thermalThrottlingDetected", run.thermalThrottlingDetected)
        json.put("stabilityRating", run.stabilityRating)

        run.cpuResult?.let { cpu ->
            val cpuObj = JSONObject()
            cpuObj.put("singleCoreScore", cpu.singleCoreScore)
            cpuObj.put("multiCoreScore", cpu.multiCoreScore)
            cpuObj.put("singleCoreMops", cpu.singleCoreMops)
            cpuObj.put("multiCoreMops", cpu.multiCoreMops)
            cpuObj.put("coreCount", cpu.coreCount)
            cpuObj.put("scalingEfficiency", cpu.scalingEfficiency)
            cpuObj.put("matrixGflops", cpu.matrixGflops)
            cpuObj.put("cryptoHashMbps", cpu.cryptoHashMbps)
            cpuObj.put("totalCpuScore", cpu.totalCpuScore)
            json.put("cpu", cpuObj)
        }

        run.ramResult?.let { ram ->
            val ramObj = JSONObject()
            ramObj.put("readBandwidthGbps", ram.readBandwidthGbps)
            ramObj.put("writeBandwidthGbps", ram.writeBandwidthGbps)
            ramObj.put("accessLatencyNs", ram.accessLatencyNs)
            ramObj.put("ramScore", ram.ramScore)
            json.put("ram", ramObj)
        }

        run.diskResult?.let { disk ->
            val diskObj = JSONObject()
            diskObj.put("seqWriteMbps", disk.seqWriteMbps)
            diskObj.put("seqReadMbps", disk.seqReadMbps)
            diskObj.put("randomWriteIops", disk.randomWriteIops)
            diskObj.put("randomReadIops", disk.randomReadIops)
            diskObj.put("diskScore", disk.diskScore)
            json.put("disk", diskObj)
        }

        run.gpuResult?.let { gpu ->
            val gpuObj = JSONObject()
            gpuObj.put("avgFps", gpu.avgFps)
            gpuObj.put("low1Fps", gpu.low1Fps)
            gpuObj.put("low01Fps", gpu.low01Fps)
            gpuObj.put("frameStabilityPercent", gpu.frameStabilityPercent)
            gpuObj.put("particleCount", gpu.particleCount)
            gpuObj.put("gpuScore", gpu.gpuScore)
            json.put("gpu", gpuObj)
        }

        return json.toString(2)
    }
}
