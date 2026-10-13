package com.example

import com.example.benchmark.GpuBenchmarkEngine
import com.example.data.BenchmarkExportManager
import com.example.data.PresetHardwareDatabase
import com.example.model.BenchmarkMode
import com.example.model.BenchmarkRun
import com.example.model.BenchmarkType
import com.example.model.CpuResult
import com.example.model.GpuResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BenchmarkLogicTest {

    @Test
    fun testGpuFrameTimingCalculations() {
        val engine = GpuBenchmarkEngine()
        engine.reset(particleCount = 2000)

        var timeNanos = 1_000_000_000L
        // Simulate 60 frames at ~16.6ms
        for (i in 0 until 60) {
            val delta = 16_666_667L
            timeNanos += delta
            engine.recordFrame(timeNanos)
        }

        val low1 = engine.calculate1PercentLow()
        assertTrue("1% low should be positive", low1 > 0f)

        val result = engine.finalizeResult()
        assertEquals(2000, result.particleCount)
        assertTrue("GPU score should be calculated", result.gpuScore > 0)
    }

    @Test
    fun testPresetHardwareTiers() {
        val tiers = PresetHardwareDatabase.hardwareTiers
        assertTrue("Should have multiple hardware tiers", tiers.size >= 5)

        val sPlus = tiers.find { it.tierBadge == "S+" }
        assertNotNull(sPlus)
        assertTrue(sPlus!!.overallScore > 12000)

        val (badge, label) = PresetHardwareDatabase.getTierForScore(14500)
        assertEquals("S+", badge)
    }

    @Test
    fun testBenchmarkExportToJson() {
        val run = BenchmarkRun(
            id = 12345L,
            timestamp = 1700000000000L,
            type = BenchmarkType.TOGETHER_STRESS,
            mode = BenchmarkMode.STANDARD,
            overallScore = 10500,
            cpuResult = CpuResult(singleCoreScore = 3200, multiCoreScore = 4100, totalCpuScore = 3800),
            gpuResult = GpuResult(avgFps = 59.8f, low1Fps = 55.2f, gpuScore = 4200),
            deviceModel = "Pixel 9 Pro",
            deviceManufacturer = "Google",
            osVersion = "Android 15"
        )

        val json = BenchmarkExportManager.exportToJson(run)
        assertTrue(json.contains("\"schemaVersion\": \"2.1.0\""))
        assertTrue(json.contains("\"overallScore\": 10500"))
        assertTrue(json.contains("\"benchmarkType\": \"TOGETHER_STRESS\""))
        assertTrue(json.contains("\"Pixel 9 Pro\""))
    }

    @Test
    fun testLoadPresetsIntensity() {
        val light = com.example.model.LoadPreset.LIGHT
        val medium = com.example.model.LoadPreset.MEDIUM
        val heavy = com.example.model.LoadPreset.HEAVY

        assertTrue(light.baseParticleCount < medium.baseParticleCount)
        assertTrue(medium.baseParticleCount < heavy.baseParticleCount)
        assertTrue(light.mathIntensity < heavy.mathIntensity)
    }

    @Test
    fun testUserProfileEntityMapping() {
        val profile = com.example.model.UserProfile(
            id = 42L,
            username = "overclocker_99",
            displayName = "Alex",
            deviceAlias = "Snapdragon Lab"
        )
        val entity = com.example.data.UserProfileEntity.fromUserProfile(profile, isCurrent = true)
        assertEquals(42L, entity.id)
        assertEquals("overclocker_99", entity.username)
        val back = entity.toUserProfile()
        assertEquals("Snapdragon Lab", back.deviceAlias)
    }
}

