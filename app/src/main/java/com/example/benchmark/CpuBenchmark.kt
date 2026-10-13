package com.example.benchmark

import com.example.model.CpuResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CpuBenchmark {

    suspend fun runBenchmark(
        durationSec: Int = 10,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): CpuResult = withContext(Dispatchers.Default) {
        val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        
        // Phase 1: Single-core workload
        onProgress(0.1f, "Running Single-Core Math & Sieve...")
        val singleCoreStart = System.nanoTime()
        val singleWorkloadOps = runSingleCoreWorkload(durationSec = (durationSec * 0.4).coerceAtLeast(2.0))
        val singleCoreDurationSec = (System.nanoTime() - singleCoreStart) / 1_000_000_000.0
        val singleCoreMops = (singleWorkloadOps / (singleCoreDurationSec.coerceAtLeast(0.001))) / 1_000_000.0

        // Phase 2: Matrix Multiplication & Float GFLOPS
        onProgress(0.4f, "Evaluating Matrix Linear Algebra...")
        val matrixGflops = measureMatrixGflops()

        // Phase 3: Cryptographic Hashing Throughput
        onProgress(0.6f, "Running SHA-256 Hashing Throughput...")
        val cryptoMbps = measureCryptoThroughput()

        // Phase 4: Multi-core parallel saturation
        onProgress(0.75f, "Saturating All $coreCount Cores...")
        val multiCoreStart = System.nanoTime()
        val multiCoreOps = runMultiCoreWorkload(coreCount, durationSec = (durationSec * 0.5).coerceAtLeast(2.0))
        val multiCoreDurationSec = (System.nanoTime() - multiCoreStart) / 1_000_000_000.0
        val multiCoreMops = (multiCoreOps / (multiCoreDurationSec.coerceAtLeast(0.001))) / 1_000_000.0

        onProgress(1.0f, "Computing CPU Points...")

        val scalingEfficiency = if (singleCoreMops > 0 && coreCount > 0) {
            ((multiCoreMops / (singleCoreMops * coreCount)).toFloat()).coerceIn(0.2f, 1.0f)
        } else 0.5f

        val singleCoreScore = ((singleCoreMops * 18.0) + (matrixGflops * 45.0) + (cryptoMbps * 3.5)).toInt()
        val multiCoreScore = ((multiCoreMops * 7.5) + (matrixGflops * coreCount * 22.0)).toInt()
        val totalCpuScore = ((singleCoreScore * 0.35) + (multiCoreScore * 0.65)).toInt()

        CpuResult(
            singleCoreScore = singleCoreScore.coerceAtLeast(150),
            multiCoreScore = multiCoreScore.coerceAtLeast(350),
            singleCoreMops = (singleCoreMops * 100).toInt() / 100.0,
            multiCoreMops = (multiCoreMops * 100).toInt() / 100.0,
            scalingEfficiency = (scalingEfficiency * 100).toInt() / 100f,
            coreCount = coreCount,
            matrixGflops = (matrixGflops * 100).toInt() / 100.0,
            cryptoHashMbps = (cryptoMbps * 100).toInt() / 100.0,
            totalCpuScore = totalCpuScore.coerceAtLeast(400)
        )
    }

    private fun runSingleCoreWorkload(durationSec: Double): Long {
        val targetNanos = (durationSec * 1_000_000_000).toLong()
        val startTime = System.nanoTime()
        var ops = 0L

        while (System.nanoTime() - startTime < targetNanos) {
            // Prime Sieve segment
            var count = 0
            val limit = 8000
            val primes = BooleanArray(limit) { true }
            for (p in 2 until sqrt(limit.toDouble()).toInt()) {
                if (primes[p]) {
                    var i = p * p
                    while (i < limit) {
                        primes[i] = false
                        i += p
                    }
                }
            }
            for (i in 2 until limit) {
                if (primes[i]) count++
            }
            ops += (limit * 2)

            // Transcendental math block
            var acc = 0.0
            for (j in 0 until 500) {
                acc += sin(j.toDouble()) * cos(j.toDouble())
            }
            ops += 1000L
        }
        return ops
    }

    private suspend fun runMultiCoreWorkload(cores: Int, durationSec: Double): Long = coroutineScope {
        val targetNanos = (durationSec * 1_000_000_000).toLong()
        val deferreds = (0 until cores).map { workerId ->
            async(Dispatchers.Default) {
                val start = System.nanoTime()
                var ops = 0L
                while (System.nanoTime() - start < targetNanos) {
                    // Parallel fractal iteration & Ray-sphere calculation
                    for (x in 0 until 400) {
                        val cx = (x - 200) / 100.0
                        val cy = (workerId - 4) / 10.0
                        var zx = 0.0
                        var zy = 0.0
                        var iter = 0
                        while (zx * zx + zy * zy < 4.0 && iter < 80) {
                            val temp = zx * zx - zy * zy + cx
                            zy = 2.0 * zx * zy + cy
                            zx = temp
                            iter++
                        }
                        ops += iter
                    }
                }
                ops
            }
        }
        deferreds.awaitAll().sum()
    }

    private fun measureMatrixGflops(): Double {
        val size = 120
        val a = Array(size) { FloatArray(size) { 1.05f } }
        val b = Array(size) { FloatArray(size) { 0.95f } }
        val c = Array(size) { FloatArray(size) }

        val start = System.nanoTime()
        val iterations = 8
        for (it in 0 until iterations) {
            for (i in 0 until size) {
                val aRow = a[i]
                val cRow = c[i]
                for (k in 0 until size) {
                    val bRow = b[k]
                    val aVal = aRow[k]
                    for (j in 0 until size) {
                        cRow[j] += aVal * bRow[j]
                    }
                }
            }
        }
        val elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0
        val flops = 2.0 * size * size * size * iterations
        return (flops / elapsedSec) / 1_000_000_000.0
    }

    private fun measureCryptoThroughput(): Double {
        val md = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024) { (it % 127).toByte() }
        val start = System.nanoTime()
        val targetNanos = 600_000_000L // 600ms
        var totalBytes = 0L

        while (System.nanoTime() - start < targetNanos) {
            md.update(buffer)
            md.digest()
            totalBytes += buffer.size
        }
        val elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0
        return (totalBytes / elapsedSec) / (1024.0 * 1024.0) // MB/s
    }
}
