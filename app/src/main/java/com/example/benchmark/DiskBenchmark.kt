package com.example.benchmark

import android.content.Context
import com.example.model.DiskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.Random

class DiskBenchmark(private val context: Context) {

    suspend fun runBenchmark(
        durationSec: Int = 10,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): DiskResult = withContext(Dispatchers.IO) {
        val testFile = File(context.cacheDir, "hyperbench_io_test.dat")
        val chunkSize = 512 * 1024 // 512 KB per write
        val totalFileSize = 24 * 1024 * 1024 // 24 MB test file
        val chunk = ByteArray(chunkSize) { (it % 255).toByte() }

        var seqWriteMbps = 0.0
        var seqReadMbps = 0.0
        var randomWriteIops = 0
        var randomReadIops = 0

        try {
            // Phase 1: Sequential Write
            onProgress(0.15f, "Testing Sequential Storage Write...")
            val writeStart = System.nanoTime()
            FileOutputStream(testFile).use { fos ->
                var written = 0
                while (written < totalFileSize) {
                    fos.write(chunk)
                    written += chunkSize
                }
                fos.fd.sync() // Ensure flushed to underlying flash block
            }
            val writeElapsedSec = (System.nanoTime() - writeStart) / 1_000_000_000.0
            seqWriteMbps = (totalFileSize / (1024.0 * 1024.0)) / writeElapsedSec.coerceAtLeast(0.001)

            // Phase 2: Sequential Read
            onProgress(0.45f, "Testing Sequential Storage Read...")
            val readBuffer = ByteArray(chunkSize)
            val readStart = System.nanoTime()
            FileInputStream(testFile).use { fis ->
                var readTotal = 0
                while (readTotal < totalFileSize) {
                    val bytesRead = fis.read(readBuffer)
                    if (bytesRead <= 0) break
                    readTotal += bytesRead
                }
            }
            val readElapsedSec = (System.nanoTime() - readStart) / 1_000_000_000.0
            seqReadMbps = (totalFileSize / (1024.0 * 1024.0)) / readElapsedSec.coerceAtLeast(0.001)

            // Phase 3: Random 4KB Write IOPS
            onProgress(0.70f, "Testing 4K Random Write IOPS...")
            val random = Random(42)
            val blockSize = 4096
            val numBlocks = (totalFileSize / blockSize).coerceAtLeast(1)
            val randomBuffer = ByteArray(blockSize) { (it % 128).toByte() }

            val randWriteStart = System.nanoTime()
            val randWriteTargetNanos = 1_500_000_000L // 1.5s test window
            var writeOps = 0

            RandomAccessFile(testFile, "rw").use { raf ->
                while (System.nanoTime() - randWriteStart < randWriteTargetNanos) {
                    val targetBlock = random.nextInt(numBlocks)
                    raf.seek(targetBlock.toLong() * blockSize)
                    raf.write(randomBuffer)
                    writeOps++
                }
                raf.fd.sync()
            }
            val randWriteElapsedSec = (System.nanoTime() - randWriteStart) / 1_000_000_000.0
            randomWriteIops = (writeOps / randWriteElapsedSec.coerceAtLeast(0.001)).toInt()

            // Phase 4: Random 4KB Read IOPS
            onProgress(0.85f, "Testing 4K Random Read IOPS...")
            val randReadStart = System.nanoTime()
            val randReadTargetNanos = 1_500_000_000L // 1.5s
            var readOps = 0

            RandomAccessFile(testFile, "r").use { raf ->
                while (System.nanoTime() - randReadStart < randReadTargetNanos) {
                    val targetBlock = random.nextInt(numBlocks)
                    raf.seek(targetBlock.toLong() * blockSize)
                    raf.read(randomBuffer)
                    readOps++
                }
            }
            val randReadElapsedSec = (System.nanoTime() - randReadStart) / 1_000_000_000.0
            randomReadIops = (readOps / randReadElapsedSec.coerceAtLeast(0.001)).toInt()

        } finally {
            if (testFile.exists()) {
                testFile.delete()
            }
        }

        onProgress(1.0f, "Evaluating Storage Points...")

        // Formula for Disk Score:
        // Modern UFS 2.2 / 3.1 / 4.0 flash storage scoring
        val diskScore = ((seqReadMbps * 1.8) + (seqWriteMbps * 2.2) + (randomWriteIops * 0.12) + (randomReadIops * 0.15)).toInt()

        DiskResult(
            seqWriteMbps = (seqWriteMbps * 10).toInt() / 10.0,
            seqReadMbps = (seqReadMbps * 10).toInt() / 10.0,
            randomWriteIops = randomWriteIops,
            randomReadIops = randomReadIops,
            diskScore = diskScore.coerceAtLeast(150)
        )
    }
}
