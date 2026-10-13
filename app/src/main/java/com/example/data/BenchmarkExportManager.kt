package com.example.data

import com.example.model.BenchmarkRun
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BenchmarkExportManager {

    private const val SCHEMA_VERSION = "2.1.0"

    fun exportToJson(run: BenchmarkRun): String {
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(run.timestamp))
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"schemaVersion\": \"$SCHEMA_VERSION\",\n")
        sb.append("  \"app\": \"HyperBench\",\n")
        sb.append("  \"id\": ${run.id},\n")
        sb.append("  \"timestamp\": ${run.timestamp},\n")
        sb.append("  \"dateFormatted\": \"$dateFormatted\",\n")
        sb.append("  \"deviceModel\": \"${run.deviceModel}\",\n")
        sb.append("  \"deviceManufacturer\": \"${run.deviceManufacturer}\",\n")
        sb.append("  \"osVersion\": \"${run.osVersion}\",\n")
        sb.append("  \"benchmarkType\": \"${run.type.name}\",\n")
        sb.append("  \"benchmarkMode\": \"${run.mode.name}\",\n")
        sb.append("  \"durationSec\": ${run.durationSec},\n")
        sb.append("  \"overallScore\": ${run.overallScore},\n")
        sb.append("  \"thermalThrottlingDetected\": ${run.thermalThrottlingDetected},\n")
        sb.append("  \"stabilityRating\": \"${run.stabilityRating}\"")

        run.cpuResult?.let { cpu ->
            sb.append(",\n  \"cpu\": {\n")
            sb.append("    \"singleCoreScore\": ${cpu.singleCoreScore},\n")
            sb.append("    \"multiCoreScore\": ${cpu.multiCoreScore},\n")
            sb.append("    \"singleCoreMops\": ${cpu.singleCoreMops},\n")
            sb.append("    \"multiCoreMops\": ${cpu.multiCoreMops},\n")
            sb.append("    \"coreCount\": ${cpu.coreCount},\n")
            sb.append("    \"scalingEfficiency\": ${cpu.scalingEfficiency},\n")
            sb.append("    \"matrixGflops\": ${cpu.matrixGflops},\n")
            sb.append("    \"cryptoHashMbps\": ${cpu.cryptoHashMbps},\n")
            sb.append("    \"totalCpuScore\": ${cpu.totalCpuScore}\n")
            sb.append("  }")
        }

        run.ramResult?.let { ram ->
            sb.append(",\n  \"ram\": {\n")
            sb.append("    \"readBandwidthGbps\": ${ram.readBandwidthGbps},\n")
            sb.append("    \"writeBandwidthGbps\": ${ram.writeBandwidthGbps},\n")
            sb.append("    \"accessLatencyNs\": ${ram.accessLatencyNs},\n")
            sb.append("    \"ramScore\": ${ram.ramScore}\n")
            sb.append("  }")
        }

        run.diskResult?.let { disk ->
            sb.append(",\n  \"disk\": {\n")
            sb.append("    \"seqWriteMbps\": ${disk.seqWriteMbps},\n")
            sb.append("    \"seqReadMbps\": ${disk.seqReadMbps},\n")
            sb.append("    \"randomWriteIops\": ${disk.randomWriteIops},\n")
            sb.append("    \"randomReadIops\": ${disk.randomReadIops},\n")
            sb.append("    \"diskScore\": ${disk.diskScore}\n")
            sb.append("  }")
        }

        run.gpuResult?.let { gpu ->
            sb.append(",\n  \"gpu\": {\n")
            sb.append("    \"avgFps\": ${gpu.avgFps},\n")
            sb.append("    \"low1Fps\": ${gpu.low1Fps},\n")
            sb.append("    \"low01Fps\": ${gpu.low01Fps},\n")
            sb.append("    \"frameStabilityPercent\": ${gpu.frameStabilityPercent},\n")
            sb.append("    \"particleCount\": ${gpu.particleCount},\n")
            sb.append("    \"gpuScore\": ${gpu.gpuScore}\n")
            sb.append("  }")
        }

        sb.append("\n}")
        return sb.toString()
    }
}
