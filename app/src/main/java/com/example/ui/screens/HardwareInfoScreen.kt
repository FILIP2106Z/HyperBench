package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HardwareProfile
import com.example.ui.AppScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HardwareInfoScreen(
    profile: HardwareProfile?,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigate(AppScreen.HOME) },
                    modifier = Modifier.testTag("specs_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Text(
                    text = "SYSTEM HARDWARE INSPECTOR",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        if (profile == null) {
            item {
                Text(text = "Loading hardware profile...", color = TextMuted)
            }
        } else {
            // CPU & SoC Telemetry
            item {
                HardwareSectionCard(
                    title = "PROCESSOR & COMPUTE",
                    accentColor = CyberCyan,
                    items = listOf(
                        "Device Model" to "${profile.manufacturer} ${profile.model}",
                        "Active Cores" to "${profile.availableProcessors} Logical Cores",
                        "Primary ABI" to profile.cpuAbi,
                        "Instruction Set" to if (profile.cpuAbi.contains("64")) "64-bit ARMv8/ARMv9" else "32-bit ARM",
                        "Board / Device" to profile.deviceName
                    )
                )
            }

            // Memory Breakdown
            val totalRamGb = (profile.totalRamBytes / (1024.0 * 1024.0 * 1024.0))
            val availRamGb = (profile.availRamBytes / (1024.0 * 1024.0 * 1024.0))
            val usedRamGb = totalRamGb - availRamGb
            val ramUsagePercent = ((usedRamGb / totalRamGb.coerceAtLeast(0.1)) * 100.0).toFloat()

            item {
                HardwareSectionCard(
                    title = "SYSTEM MEMORY (RAM)",
                    accentColor = Color(0xFFFF9100),
                    items = listOf(
                        "Total Physical RAM" to "%.2f GB".format(totalRamGb),
                        "Available RAM" to "%.2f GB".format(availRamGb),
                        "Used RAM" to "%.2f GB".format(usedRamGb),
                        "Memory Load" to "%.1f%%".format(ramUsagePercent)
                    ),
                    extraContent = {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { ramUsagePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFF9100),
                            trackColor = SurfaceCard
                        )
                    }
                )
            }

            // Storage Breakdown
            val totalStorageGb = (profile.totalStorageBytes / (1024.0 * 1024.0 * 1024.0))
            val freeStorageGb = (profile.freeStorageBytes / (1024.0 * 1024.0 * 1024.0))
            val usedStorageGb = totalStorageGb - freeStorageGb
            val storageUsagePercent = ((usedStorageGb / totalStorageGb.coerceAtLeast(0.1)) * 100.0).toFloat()

            item {
                HardwareSectionCard(
                    title = "INTERNAL STORAGE (FLASH)",
                    accentColor = MatrixGreen,
                    items = listOf(
                        "Total Storage Capacity" to "%.1f GB".format(totalStorageGb),
                        "Free Space" to "%.1f GB".format(freeStorageGb),
                        "Used Space" to "%.1f GB".format(usedStorageGb),
                        "Storage Load" to "%.1f%%".format(storageUsagePercent)
                    ),
                    extraContent = {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { storageUsagePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MatrixGreen,
                            trackColor = SurfaceCard
                        )
                    }
                )
            }

            // Display & Graphics
            item {
                HardwareSectionCard(
                    title = "DISPLAY & RENDERING TARGET",
                    accentColor = ElectricPurple,
                    items = listOf(
                        "Native Resolution" to profile.displayResolution,
                        "Hardware Refresh Rate" to "${profile.refreshRateHz.toInt()} Hz",
                        "Target Frame Time" to "%.2f ms".format(1000f / profile.refreshRateHz.coerceAtLeast(1f)),
                        "Hardware Acceleration" to "Enabled (Direct Canvas / Vulkan)"
                    )
                )
            }

            // Software & OS
            item {
                HardwareSectionCard(
                    title = "SOFTWARE & OS ENVIRONMENT",
                    accentColor = TextSecondary,
                    items = listOf(
                        "Android OS Version" to profile.androidVersion,
                        "API Level" to "API ${profile.apiLevel}",
                        "Benchmark Framework" to "HyperBench Core 2.1 Native",
                        "Multi-Platform Spec" to "Compliant (Schema v2.1.0)"
                    )
                )
            }
        }
    }
}

@Composable
private fun HardwareSectionCard(
    title: String,
    accentColor: Color,
    items: List<Pair<String, String>>,
    extraContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            items.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 12.sp, color = TextSecondary)
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
                if (index < items.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
            extraContent?.invoke()
        }
    }
}
