package com.example.benchmark

import com.example.model.RamResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.math.max

class RamBenchmark {

    suspend fun runBenchmark(
        durationSec: Int = 8,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): RamResult = withContext(Dispatchers.Default) {
        val bufferSizeBytes = 24 * 1024 * 1024 // 24 MB direct native byte buffer
        val directBuffer = ByteBuffer.allocateDirect(bufferSizeBytes)
        val longBuffer = directBuffer.asLongBuffer()
        val numLongs = bufferSizeBytes / 8

        // Phase 1: Sequential Write Bandwidth
        onProgress(0.15f, "Testing Sequential RAM Write Bandwidth...")
        val writeStart = System.nanoTime()
        val writeTargetNanos = ((durationSec * 0.35) * 1_000_000_000).toLong().coerceAtLeast(1_000_000_000L)
        var totalBytesWritten = 0L

        while (System.nanoTime() - writeStart < writeTargetNanos) {
            longBuffer.clear()
            var value = 0x0123456789ABCDEFL
            for (i in 0 until numLongs) {
                longBuffer.put(i, value)
                value = (value shl 1) xor (value ushr 63) xor 0x1L
            }
            totalBytesWritten += bufferSizeBytes
        }
        val writeElapsedSec = (System.nanoTime() - writeStart) / 1_000_000_000.0
        val writeGbps = (totalBytesWritten / writeElapsedSec) / (1024.0 * 1024.0 * 1024.0)

        // Phase 2: Sequential Read Bandwidth
        onProgress(0.50f, "Testing Sequential RAM Read Bandwidth...")
        val readStart = System.nanoTime()
        val readTargetNanos = ((durationSec * 0.35) * 1_000_000_000).toLong().coerceAtLeast(1_000_000_000L)
        var totalBytesRead = 0L
        var checksum = 0L

        while (System.nanoTime() - readStart < readTargetNanos) {
            longBuffer.clear()
            for (i in 0 until numLongs) {
                checksum = checksum xor longBuffer.get(i)
            }
            totalBytesRead += bufferSizeBytes
        }
        val readElapsedSec = (System.nanoTime() - readStart) / 1_000_000_000.0
        val readGbps = (totalBytesRead / readElapsedSec) / (1024.0 * 1024.0 * 1024.0)

        // Phase 3: Random Access & Cache Latency
        onProgress(0.80f, "Measuring Random Access Latency...")
        val randomStart = System.nanoTime()
        val randomTargetNanos = ((durationSec * 0.30) * 1_000_000_000).toLong().coerceAtLeast(800_000_000L)
        var randomOps = 0L
        var index = 17
        val stride = 10007 // Large prime stride to avoid L1/L2 prefetch hits

        while (System.nanoTime() - randomStart < randomTargetNanos) {
            for (step in 0 until 10_000) {
                index = (index + stride) % numLongs
                checksum = checksum xor longBuffer.get(index)
            }
            randomOps += 10_000L
        }
        val randomElapsedNanos = System.nanoTime() - randomStart
        val latencyNs = randomElapsedNanos.toDouble() / randomOps.coerceAtLeast(1L)
        val randomBytes = randomOps * 8L
        val randomThroughputGbps = (randomBytes / (randomElapsedNanos / 1_000_000_000.0)) / (1024.0 * 1024.0 * 1024.0)

        onProgress(1.0f, "Calculating RAM Points...")

        // Formula for RAM Score:
        // Scaled to modern mobile DRAM (e.g. 10-40 GB/s results in ~2,000-6,000 points)
        val ramScore = ((readGbps * 120.0) + (writeGbps * 95.0) + (randomThroughputGbps * 150.0) + max(0.0, (120.0 - latencyNs) * 10.0)).toInt()

        RamResult(
            readBandwidthGbps = (readGbps * 100).toInt() / 100.0,
            writeBandwidthGbps = (writeGbps * 100).toInt() / 100.0,
            randomThroughputGbps = (randomThroughputGbps * 100).toInt() / 100.0,
            accessLatencyNs = (latencyNs * 10).toInt() / 10.0,
            ramScore = ramScore.coerceAtLeast(250)
        )
    }
}
