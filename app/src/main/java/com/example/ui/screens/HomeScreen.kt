package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BenchmarkMode
import com.example.model.BenchmarkType
import com.example.model.HardwareProfile
import com.example.model.LiveTelemetry
import com.example.model.LoadPreset
import com.example.ui.AppScreen
import com.example.ui.UiState
import com.example.ui.components.ScoreCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    uiState: UiState,
    onSelectType: (BenchmarkType) -> Unit,
    onSelectMode: (BenchmarkMode) -> Unit,
    onSelectPreset: (LoadPreset) -> Unit,
    onSetDifficulty: (Int) -> Unit,
    onAutoBalance: () -> Unit,
    onSetParticleCount: (Int) -> Unit,
    onToggleComplexMesh: (Boolean) -> Unit,
    onStartBenchmark: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header & User Account Chip
        item {
            HeaderCard(
                profile = uiState.hardwareProfile,
                activeUserName = uiState.activeProfile.displayName,
                onOpenProfile = { onNavigate(AppScreen.USER_PROFILE) },
                onViewSpecs = { onNavigate(AppScreen.HARDWARE_SPECS) }
            )
        }

        // Live Sensors & Hardware Telemetry (Temperature, RAM, CPU, GPU, Privileged)
        item {
            LiveHardwareTelemetryCard(
                telemetry = uiState.liveTelemetry,
                onConfigureDrivers = { onNavigate(AppScreen.GPU_DRIVER_SETTINGS) }
            )
        }

        // Load Preset & Adjustable Difficulty Settings
        item {
            LoadPresetsCard(
                selectedPreset = uiState.selectedLoadPreset,
                difficulty = uiState.difficultyLevel,
                isAutoBalanced = uiState.isAutoHardwareBalanced,
                onSelectPreset = onSelectPreset,
                onSetDifficulty = onSetDifficulty,
                onAutoBalance = onAutoBalance
            )
        }

        // Quick Controls for GPU Driver & Multiplatform Architecture
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Speed,
                    title = "GPU & Driver",
                    subtitle = uiState.selectedGpuBackend.displayName,
                    accentColor = ElectricPurple,
                    onClick = { onNavigate(AppScreen.GPU_DRIVER_SETTINGS) },
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    icon = Icons.Default.Description,
                    title = "Multi-Platform",
                    subtitle = "Win / Mac / Linux Docs",
                    accentColor = CyberCyan,
                    onClick = { onNavigate(AppScreen.MULTI_PLATFORM_DOCS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Test Type Selection (Only vs Together Modes)
        item {
            Text(
                text = "BENCHMARK SUITE (ONLY & TOGETHER)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BenchmarkType.entries.forEach { type ->
                    val isSelected = uiState.selectedType == type
                    BenchmarkTypeCard(
                        type = type,
                        isSelected = isSelected,
                        onClick = { onSelectType(type) }
                    )
                }
            }
        }

        // Workload Duration Tiers
        item {
            Text(
                text = "TEST DURATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BenchmarkMode.entries.forEach { mode ->
                    val isSelected = uiState.selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.15f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) CyberCyan else DarkSurfaceVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyberCyan else TextPrimary
                            )
                            Text(
                                text = "${mode.durationSec}s",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Primary Action: Start Benchmark
        item {
            Button(
                onClick = onStartBenchmark,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_benchmark_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color(0xFF041017)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START ${uiState.selectedType.title.uppercase()} (${uiState.selectedLoadPreset.label} / ${uiState.selectedMode.durationSec}s)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Last Completed Run Quick Card
        uiState.latestCompletedRun?.let { lastRun ->
            item {
                Text(
                    text = "LATEST BENCHMARK RESULT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                ScoreCard(
                    run = lastRun,
                    modifier = Modifier.clickable { onNavigate(AppScreen.RESULTS) }
                )
            }
        }
    }
}

@Composable
private fun HeaderCard(
    profile: HardwareProfile?,
    activeUserName: String,
    onOpenProfile: () -> Unit,
    onViewSpecs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(CyberCyan, ElectricPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "HyperBench",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HYPERBENCH",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.4.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Native Hardware Benchmark Suite",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardElevated)
                            .clickable { onOpenProfile() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("user_profile_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = "Profile", tint = CyberCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardElevated)
                            .clickable { onViewSpecs() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("specs_button")
                    ) {
                        Text(text = "SPECS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            profile?.let { p ->
                val ramGb = (p.totalRamBytes / (1024.0 * 1024.0 * 1024.0)).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpecPill(label = "DEVICE", value = "${p.manufacturer} ${p.model}")
                    SpecPill(label = "CORES", value = "${p.availableProcessors} Cores")
                    SpecPill(label = "RAM", value = "$ramGb GB")
                    SpecPill(label = "REFRESH", value = "${p.refreshRateHz.toInt()} Hz")
                }
            }
        }
    }
}

@Composable
private fun LiveHardwareTelemetryCard(
    telemetry: LiveTelemetry,
    onConfigureDrivers: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Sensors",
                        tint = if (telemetry.batteryTempC > 40f) DangerRed else MatrixGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE HARDWARE METERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Privileged status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MatrixGreen.copy(alpha = 0.15f))
                        .clickable { onConfigureDrivers() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Security", tint = MatrixGreen, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = telemetry.privilegedAccess, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MatrixGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryTile("TEMP", "${telemetry.batteryTempC} °C", if (telemetry.batteryTempC > 40f) DangerRed else MatrixGreen)
                TelemetryTile("RAM USAGE", "${telemetry.ramUsedMb}/${telemetry.ramTotalMb}MB", CyberCyan)
                TelemetryTile("CPU LOAD", "${telemetry.cpuUsagePercent}%", ElectricPurple)
                TelemetryTile("GPU LOAD", "${telemetry.gpuUsagePercent}%", Color(0xFFFF9100))
            }
        }
    }
}

@Composable
private fun TelemetryTile(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
private fun LoadPresetsCard(
    selectedPreset: LoadPreset,
    difficulty: Int,
    isAutoBalanced: Boolean,
    onSelectPreset: (LoadPreset) -> Unit,
    onSetDifficulty: (Int) -> Unit,
    onAutoBalance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOAD PRESETS & DIFFICULTY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                if (isAutoBalanced) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Auto", tint = CyberCyan, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "AUTO-BALANCED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoadPreset.entries.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.18f) else SurfaceCard)
                            .border(1.5.dp, if (isSelected) CyberCyan else DarkSurfaceVariant, RoundedCornerShape(10.dp))
                            .clickable { onSelectPreset(preset) }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = preset.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyberCyan else TextPrimary
                            )
                            Text(
                                text = "${preset.baseParticleCount} pts",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Intensity Difficulty: Level $difficulty/10", fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = "Reset Auto",
                    fontSize = 10.sp,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onAutoBalance() }
                )
            }

            Slider(
                value = difficulty.toFloat(),
                onValueChange = { onSetDifficulty(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = CyberCyan,
                    activeTrackColor = CyberCyan,
                    inactiveTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, fontSize = 10.sp, color = TextMuted, maxLines = 1)
            }
        }
    }
}

@Composable
private fun SpecPill(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun BenchmarkTypeCard(
    type: BenchmarkType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (type) {
        BenchmarkType.CPU_ONLY -> Icons.Default.Memory
        BenchmarkType.GPU_ONLY -> Icons.Default.Speed
        BenchmarkType.RAM_ONLY -> Icons.Default.Layers
        BenchmarkType.DISK_ONLY -> Icons.Default.Storage
        BenchmarkType.TOGETHER_STRESS -> Icons.Default.Whatshot
    }

    val accentColor = if (type == BenchmarkType.TOGETHER_STRESS) Color(0xFFFF9100) else CyberCyan

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.12f) else DarkSurface)
            .border(
                1.5.dp,
                if (isSelected) accentColor else DarkSurfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("type_${type.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) accentColor.copy(alpha = 0.25f) else SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = type.title,
                    tint = if (isSelected) accentColor else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accentColor else TextPrimary
                    )
                    if (type == BenchmarkType.TOGETHER_STRESS) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STRESS MODE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF9100),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF9100).copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = type.subtitle,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 2
                )
            }
        }
    }
}
