package com.example.benchmark

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import com.example.model.LiveTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class PrivilegedTelemetryManager(private val context: Context) {

    private val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val memInfo = ActivityManager.MemoryInfo()

    var simulatedElevatedMode: String? = null

    // Cache the root status to avoid repeatedly calling su process on every frame
    private var isRootGrantedCached: Boolean? = null
    private var lastRootCheckTimestamp = 0L

    fun checkPrivilegedEnvironment(): String {
        simulatedElevatedMode?.let { return it }

        // 1. Check if Root is already granted
        if (isRootGrantedCached == true) {
            return "Root Superuser"
        }

        // Check if root is available and granted periodically (every 5 seconds)
        val now = System.currentTimeMillis()
        if (now - lastRootCheckTimestamp > 5000L) {
            lastRootCheckTimestamp = now
            if (isRootBinaryPresent()) {
                val granted = checkRootExecution()
                isRootGrantedCached = granted
                if (granted) return "Root Superuser"
                return "Root Available"
            }
        } else if (isRootGrantedCached == false && isRootBinaryPresent()) {
            return "Root Available"
        }

        // 2. Check Shizuku status
        if (isShizukuServiceRunning()) {
            return "Shizuku ADB"
        }
        if (isShizukuInstalled()) {
            return "Shizuku Installed"
        }

        return "Standard User"
    }

    /**
     * Checks if the su binary is found in system directories or PATH,
     * or if known root managers are installed (Magisk, KernelSU, APatch).
     */
    fun isRootBinaryPresent(): Boolean {
        val rootPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/app/Superuser.apk",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su"
        )
        if (rootPaths.any { File(it).exists() }) return true

        // Search PATH environment directories
        val envPath = System.getenv("PATH") ?: ""
        for (dir in envPath.split(":")) {
            val file = File(dir, "su")
            if (file.exists()) return true
        }

        // Check package manager for root manager apps
        val pm = context.packageManager
        val rootPackages = listOf(
            "com.topjohnwu.magisk",
            "me.weishu.kernelsu",
            "org.bndeff.apatch",
            "eu.chainfire.supersu"
        )
        for (pkg in rootPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (_: Exception) {}
        }

        return false
    }

    /**
     * Tests if root execution succeeds without freezing the UI.
     */
    fun checkRootExecution(): Boolean {
        return try {
            val process = ProcessBuilder("su", "-c", "id").start()
            val reader = process.inputStream.bufferedReader()
            val output = reader.readLine() ?: ""
            process.waitFor(1500, TimeUnit.MILLISECONDS)
            output.contains("uid=0")
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Explicitly requests root permission by running `su -c id`,
     * which prompts Magisk / KernelSU / APatch for superuser confirmation.
     */
    suspend fun requestRootPermission(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("su", "-c", "id").start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode == 0 && output.contains("uid=0")) {
                isRootGrantedCached = true
                Result.success("Superuser granted (UID: 0). Elevated privileges active.")
            } else {
                isRootGrantedCached = false
                Result.failure(Exception("Root permission was denied by superuser manager (Exit $exitCode)"))
            }
        } catch (e: Exception) {
            isRootGrantedCached = false
            Result.failure(Exception("Could not execute su command: ${e.localizedMessage}"))
        }
    }

    /**
     * Checks if Shizuku manager is installed on device.
     */
    fun isShizukuInstalled(): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks if Shizuku's ContentProvider is active and responsive.
     */
    fun isShizukuServiceRunning(): Boolean {
        if (!isShizukuInstalled()) return false
        return try {
            val uri = Uri.parse("content://moe.shizuku.privileged.api.provider")
            val bundle = context.contentResolver.call(uri, "ping", null, null)
            bundle != null
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns an intent to launch Shizuku manager app so the user can start ADB or authorize.
     */
    fun getShizukuLaunchIntent(): Intent? {
        val pm = context.packageManager
        return pm.getLaunchIntentForPackage("moe.shizuku.privileged.api")
    }

    /**
     * Reads privileged hardware sensors if Root or Shizuku is granted.
     */
    suspend fun readPrivilegedHardwareMetrics(): Map<String, String> = withContext(Dispatchers.IO) {
        val metrics = mutableMapOf<String, String>()

        // 1. Read real CPU frequencies
        try {
            val cpu0FreqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (cpu0FreqFile.exists() && cpu0FreqFile.canRead()) {
                val khz = cpu0FreqFile.readText().trim().toLongOrNull()
                if (khz != null) metrics["CPU0 Frequency"] = "${khz / 1000} MHz"
            } else if (isRootGrantedCached == true) {
                val p = ProcessBuilder("su", "-c", "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq").start()
                val line = p.inputStream.bufferedReader().readLine()?.trim()
                val khz = line?.toLongOrNull()
                if (khz != null) metrics["CPU0 Frequency"] = "${khz / 1000} MHz"
            }
        } catch (_: Exception) {}

        // 2. Read Qualcomm Adreno GPU frequency (e.g. Snapdragon 680 Adreno 610)
        try {
            val gpuFreqPaths = listOf(
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
                "/sys/kernel/gpu/gpu_clock"
            )
            for (path in gpuFreqPaths) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val raw = file.readText().trim().toLongOrNull()
                    if (raw != null) {
                        val mhz = if (raw > 1_000_000) raw / 1_000_000 else raw / 1000
                        metrics["Adreno GPU Clock"] = "$mhz MHz"
                        break
                    }
                } else if (isRootGrantedCached == true) {
                    val p = ProcessBuilder("su", "-c", "cat $path").start()
                    val line = p.inputStream.bufferedReader().readLine()?.trim()
                    val raw = line?.toLongOrNull()
                    if (raw != null) {
                        val mhz = if (raw > 1_000_000) raw / 1_000_000 else raw / 1000
                        metrics["Adreno GPU Clock"] = "$mhz MHz"
                        break
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Read Thermal Zone 0 Temperature
        try {
            val tz0 = File("/sys/class/thermal/thermal_zone0/temp")
            if (tz0.exists() && tz0.canRead()) {
                val tempRaw = tz0.readText().trim().toLongOrNull()
                if (tempRaw != null) {
                    val c = if (tempRaw > 1000) tempRaw / 1000.0f else tempRaw.toFloat()
                    metrics["Thermal Zone 0"] = "%.1f °C".format(c)
                }
            }
        } catch (_: Exception) {}

        metrics
    }

    fun readLiveTelemetry(activeFps: Float = 60f): LiveTelemetry {
        // 1. Battery / Device Temperature
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320
        val tempC = tempRaw / 10.0f

        // 2. RAM Usage
        actManager?.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = (totalMb - availMb).coerceAtLeast(0)

        // 3. CPU and GPU utilization estimation
        val cpuUsage = ((usedMb.toFloat() / totalMb.coerceAtLeast(1L)) * 40f + 15f).coerceIn(5f, 98f)
        val gpuTargetMs = 16.67f
        val frameMs = (1000f / activeFps.coerceAtLeast(1f))
        val gpuUsage = ((frameMs / gpuTargetMs) * 75f).coerceIn(10f, 99f)

        return LiveTelemetry(
            batteryTempC = (tempC * 10).toInt() / 10f,
            ramUsedMb = usedMb,
            ramTotalMb = totalMb,
            cpuUsagePercent = (cpuUsage * 10).toInt() / 10f,
            gpuUsagePercent = (gpuUsage * 10).toInt() / 10f,
            privilegedAccess = checkPrivilegedEnvironment()
        )
    }
}
