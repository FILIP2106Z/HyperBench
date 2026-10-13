package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.benchmark.GpuBenchmarkEngine
import com.example.model.BenchmarkType
import com.example.model.FrameTimingStats
import com.example.ui.AppScreen
import com.example.ui.UiState
import com.example.ui.components.FrameTimingHud
import com.example.ui.components.GaugeMeter
import com.example.ui.components.GpuBenchmarkRenderer
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActiveBenchmarkScreen(
    uiState: UiState,
    gpuEngine: GpuBenchmarkEngine,
    onFrameStats: (FrameTimingStats) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onCancel()
    }

    val isRenderingGpu = uiState.selectedType == BenchmarkType.GPU_ONLY ||
            uiState.selectedType == BenchmarkType.TOGETHER_STRESS

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. Rendering Viewport (GPU or Synthetic Load Visualizer)
        if (isRenderingGpu) {
            GpuBenchmarkRenderer(
                engine = gpuEngine,
                particleCount = uiState.gpuParticleCount,
                isComplexMode = uiState.gpuIsComplexMesh,
                modifier = Modifier.fillMaxSize(),
                onFrameStats = onFrameStats
            )
        } else {
            // Dedicated visualizer for CPU / RAM / Disk tests
            SubsystemLoadVisualizer(
                type = uiState.selectedType,
                progress = uiState.progress,
                currentStage = uiState.currentStage,
                liveStatText = uiState.liveStatsText
            )
        }

        // 2. Top Header HUD with Progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.88f))
                    .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = uiState.selectedType.title.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (uiState.selectedType == BenchmarkType.TOGETHER_STRESS) Color(0xFFFF9100) else CyberCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = uiState.currentStage,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .testTag("cancel_benchmark_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { uiState.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (uiState.selectedType == BenchmarkType.TOGETHER_STRESS) Color(0xFFFF9100) else CyberCyan,
                trackColor = DarkSurfaceVariant
            )

            // Live telemetry status banner (especially in Together stress mode)
            if (uiState.liveStatsText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0C1424).copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiState.liveStatsText,
                        fontSize = 11.sp,
                        color = Color(0xFF70F5FF),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 3. Bottom Real-Time Telemetry HUD (Frame Timing Analysis)
        if (isRenderingGpu) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                FrameTimingHud(
                    stats = uiState.activeFrameStats,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SubsystemLoadVisualizer(
    type: BenchmarkType,
    progress: Float,
    currentStage: String,
    liveStatText: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            GaugeMeter(
                value = progress * 100f,
                maxValue = 100f,
                size = 220.dp,
                title = "PROGRESS",
                unit = "%"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentStage,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = liveStatText.ifEmpty { "Executing high-throughput native loops..." },
                fontSize = 12.sp,
                color = CyberCyan,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
