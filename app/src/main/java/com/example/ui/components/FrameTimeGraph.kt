package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FrameTimingStats
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max

@Composable
fun FrameTimingHud(
    stats: FrameTimingStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface.copy(alpha = 0.92f))
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Top telemetry header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FRAME TIMING TELEMETRY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Janks: ${stats.jankCount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (stats.jankCount > 5) DangerRed else MatrixGreen,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4 KPI metrics: FPS, FrameTime, 1% Low, 0.1% Low
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FrameMetricChip(
                label = "AVG FPS",
                value = stats.avgFps.toInt().toString(),
                unit = "fps",
                highlightColor = MatrixGreen
            )
            FrameMetricChip(
                label = "FRAME TIME",
                value = stats.currentFrameTimeMs.toString(),
                unit = "ms",
                highlightColor = CyberCyan
            )
            FrameMetricChip(
                label = "1% LOW",
                value = stats.low1Percent.toInt().toString(),
                unit = "fps",
                highlightColor = Color(0xFFFF9100)
            )
            FrameMetricChip(
                label = "0.1% LOW",
                value = stats.low01Percent.toInt().toString(),
                unit = "fps",
                highlightColor = DangerRed
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Real-time scrolling frame time chart
        Text(
            text = "Frame Time History (ms) — Target 16.6ms (60 FPS)",
            fontSize = 10.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(4.dp))

        FrameTimeSparkline(
            history = stats.frameTimeHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Frame distribution breakdown
        FrameDistributionBar(distribution = stats.frameDistribution)
    }
}

@Composable
private fun FrameMetricChip(
    label: String,
    value: String,
    unit: String,
    highlightColor: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = highlightColor
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                fontSize = 9.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun FrameTimeSparkline(
    history: List<Float>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF070B12))
            .border(1.dp, Color(0xFF1E2A3E), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Target reference line: 16.6ms (60 FPS)
            val maxMs = 40f
            val target60Y = h - ((16.67f / maxMs) * h).coerceIn(0f, h)
            val target120Y = h - ((8.33f / maxMs) * h).coerceIn(0f, h)

            // 120Hz guideline (8.3ms)
            drawLine(
                color = Color(0x3300E676),
                start = Offset(0f, target120Y),
                end = Offset(w, target120Y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // 60Hz target line (16.6ms)
            drawLine(
                color = Color(0x66FF9100),
                start = Offset(0f, target60Y),
                end = Offset(w, target60Y),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            if (history.size < 2) return@Canvas

            val stepX = w / (history.size - 1).coerceAtLeast(1)
            val path = Path()

            history.forEachIndexed { index, ms ->
                val x = index * stepX
                val clampedMs = ms.coerceIn(0f, maxMs)
                val y = h - ((clampedMs / maxMs) * h)

                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                // Dot spikes (>16.6ms)
                if (ms > 16.67f) {
                    val dotColor = if (ms > 33.3f) DangerRed else Color(0xFFFF9100)
                    drawCircle(
                        color = dotColor,
                        radius = 2.5f,
                        center = Offset(x, y)
                    )
                }
            }

            drawPath(
                path = path,
                color = CyberCyan,
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun FrameDistributionBar(distribution: Map<String, Int>) {
    val total = max(1, distribution.values.sum())
    val under8 = distribution["< 8.3ms"] ?: 0
    val to16 = distribution["8.3 - 16.6ms"] ?: 0
    val to33 = distribution["16.6 - 33.3ms"] ?: 0
    val over33 = distribution["> 33.3ms"] ?: 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "<8.3ms: ${(under8 * 100 / total)}%",
                fontSize = 9.sp,
                color = MatrixGreen
            )
            Text(
                text = "8-16ms: ${(to16 * 100 / total)}%",
                fontSize = 9.sp,
                color = CyberCyan
            )
            Text(
                text = "16-33ms: ${(to33 * 100 / total)}%",
                fontSize = 9.sp,
                color = Color(0xFFFF9100)
            )
            Text(
                text = ">33ms: ${(over33 * 100 / total)}%",
                fontSize = 9.sp,
                color = DangerRed
            )
        }
    }
}
