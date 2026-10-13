package com.example.benchmark

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.model.LiveTelemetry
import java.io.File

class PrivilegedTelemetryManager(private val context: Context) {

    private val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val memInfo = ActivityManager.MemoryInfo()

    var simulatedElevatedMode: String? = null

    fun checkPrivilegedEnvironment(): String {
        simulatedElevatedMode?.let { return it }

        // 1. Check Root binaries
        val rootPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/app/Superuser.apk"
        )
        val hasRoot = rootPaths.any { File(it).exists() }
        if (hasRoot) return "Root Superuser"

        // 2. Check Shizuku package presence
        val pm = context.packageManager
        val hasShizuku = try {
            pm.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: Exception) {
            false
        }
        if (hasShizuku) return "Shizuku ADB"

        return "Standard User"
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
