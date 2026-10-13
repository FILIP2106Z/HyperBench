package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.PresetHardwareDatabase
import com.example.model.BenchmarkRun
import com.example.model.HardwareTier
import com.example.ui.AppScreen
import com.example.ui.components.HardwareRadarChart
import com.example.ui.components.RadarAxis
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
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
fun CompareScreen(
    currentRun: BenchmarkRun?,
    selectedTier: HardwareTier,
    onSelectTier: (HardwareTier) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    val deviceOverall = currentRun?.overallScore ?: 7200
    val deviceCpu = currentRun?.cpuResult?.totalCpuScore ?: 2600
    val deviceGpu = currentRun?.gpuResult?.gpuScore ?: 2900
    val deviceRam = currentRun?.ramResult?.ramScore ?: 2300
    val deviceDisk = currentRun?.diskResult?.diskScore ?: 2200
    val deviceStability = if (currentRun?.thermalThrottlingDetected == true) 0.65f else 0.95f

    // Radar chart axes normalized to max reference values
    val maxCpu = 8500f
    val maxGpu = 9500f
    val maxRam = 7000f
    val maxDisk = 7500f

    val radarAxes = listOf(
        RadarAxis("CPU", (deviceCpu / maxCpu).coerceIn(0.1f, 1f), (selectedTier.cpuScore / maxCpu).coerceIn(0.1f, 1f)),
        RadarAxis("GPU", (deviceGpu / maxGpu).coerceIn(0.1f, 1f), (selectedTier.gpuScore / maxGpu).coerceIn(0.1f, 1f)),
        RadarAxis("RAM", (deviceRam / maxRam).coerceIn(0.1f, 1f), (selectedTier.ramScore / maxRam).coerceIn(0.1f, 1f)),
        RadarAxis("STORAGE", (deviceDisk / maxDisk).coerceIn(0.1f, 1f), (selectedTier.diskScore / maxDisk).coerceIn(0.1f, 1f)),
        RadarAxis("STABILITY", deviceStability, 0.90f)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Navigation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigate(AppScreen.HOME) },
                    modifier = Modifier.testTag("compare_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "HARDWARE COMPARISON",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Horizontal Hardware Tier Selector
        item {
            Text(
                text = "SELECT COMPARISON BASELINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PresetHardwareDatabase.hardwareTiers) { tier ->
                    val isSelected = tier.id == selectedTier.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.15f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) CyberCyan else DarkSurfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectTier(tier) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("tier_${tier.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tier.tierBadge,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) CyberCyan else TextPrimary
                            )
                            Text(
                                text = tier.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                            Text(
                                text = "%,d pts".format(tier.overallScore),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Radar Comparison Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MULTI-AXIS RADAR ANALYSIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )

                        // Legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberCyan))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Your Device", fontSize = 10.sp, color = TextPrimary)

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ElectricPurple))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = selectedTier.tierBadge, fontSize = 10.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HardwareRadarChart(
                        axes = radarAxes,
                        size = 230.dp
                    )
                }
            }
        }

        // Delta Scores Breakdown
        item {
            Text(
                text = "PERFORMANCE DELTA VS ${selectedTier.tierBadge} (${selectedTier.name})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DeltaMetricRow("OVERALL SYSTEM", deviceOverall, selectedTier.overallScore)
                DeltaMetricRow("CPU COMPUTE", deviceCpu, selectedTier.cpuScore)
                DeltaMetricRow("GPU & RENDERING", deviceGpu, selectedTier.gpuScore)
                DeltaMetricRow("RAM BANDWIDTH", deviceRam, selectedTier.ramScore)
                DeltaMetricRow("STORAGE I/O", deviceDisk, selectedTier.diskScore)
            }
        }

        // Hardware Baseline Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "REFERENCE SPECS: ${selectedTier.chipset}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedTier.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DeltaMetricRow(
    title: String,
    deviceScore: Int,
    referenceScore: Int
) {
    val diff = deviceScore - referenceScore
    val percent = ((diff.toDouble() / referenceScore.coerceAtLeast(1)) * 100.0)
    val isAhead = diff >= 0
    val deltaColor = if (isAhead) MatrixGreen else DangerRed
    val sign = if (isAhead) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            Text(
                text = "%,d vs %,d pts".format(deviceScore, referenceScore),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(deltaColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$sign%.1f%%".format(percent),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = deltaColor
            )
        }
    }
}
