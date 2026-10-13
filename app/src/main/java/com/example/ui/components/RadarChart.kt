package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.TextMuted
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class RadarAxis(
    val label: String,
    val currentValue: Float, // Normalized 0f..1f
    val referenceValue: Float // Normalized 0f..1f
)

@Composable
fun HardwareRadarChart(
    axes: List<RadarAxis>,
    size: Dp = 230.dp,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = (this.size.minDimension / 2f) * 0.72f
            val count = axes.size
            if (count < 3) return@Canvas

            val angleStep = (2 * PI) / count

            // Draw concentric background web (4 levels)
            for (level in 1..4) {
                val r = (maxRadius * level) / 4f
                val ringPath = Path()
                for (i in 0 until count) {
                    val angle = (i * angleStep) - (PI / 2.0)
                    val x = center.x + (r * cos(angle)).toFloat()
                    val y = center.y + (r * sin(angle)).toFloat()
                    if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                }
                ringPath.close()
                drawPath(
                    path = ringPath,
                    color = DarkSurfaceVariant,
                    style = Stroke(width = 1f)
                )
            }

            // Draw radiating axis lines
            for (i in 0 until count) {
                val angle = (i * angleStep) - (PI / 2.0)
                val endX = center.x + (maxRadius * cos(angle)).toFloat()
                val endY = center.y + (maxRadius * sin(angle)).toFloat()
                drawLine(
                    color = DarkSurfaceVariant,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1f
                )

                // Axis label
                val labelX = center.x + ((maxRadius + 18.dp.toPx()) * cos(angle)).toFloat()
                val labelY = center.y + ((maxRadius + 18.dp.toPx()) * sin(angle)).toFloat()
                val layoutResult = textMeasurer.measure(
                    text = axes[i].label,
                    style = TextStyle(
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(
                        labelX - layoutResult.size.width / 2f,
                        labelY - layoutResult.size.height / 2f
                    )
                )
            }

            // 1. Draw Reference Platform Polygon (Purple)
            val refPath = Path()
            for (i in 0 until count) {
                val r = maxRadius * axes[i].referenceValue.coerceIn(0.1f, 1f)
                val angle = (i * angleStep) - (PI / 2.0)
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) refPath.moveTo(x, y) else refPath.lineTo(x, y)
            }
            refPath.close()
            drawPath(
                path = refPath,
                color = ElectricPurple.copy(alpha = 0.25f),
                style = Fill
            )
            drawPath(
                path = refPath,
                color = ElectricPurple,
                style = Stroke(width = 1.8f)
            )

            // 2. Draw Current Device Polygon (Cyan)
            val curPath = Path()
            for (i in 0 until count) {
                val r = maxRadius * axes[i].currentValue.coerceIn(0.1f, 1f)
                val angle = (i * angleStep) - (PI / 2.0)
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) curPath.moveTo(x, y) else curPath.lineTo(x, y)
            }
            curPath.close()
            drawPath(
                path = curPath,
                color = CyberCyan.copy(alpha = 0.35f),
                style = Fill
            )
            drawPath(
                path = curPath,
                color = CyberCyan,
                style = Stroke(width = 2.4f)
            )

            // Vertex points
            for (i in 0 until count) {
                val r = maxRadius * axes[i].currentValue.coerceIn(0.1f, 1f)
                val angle = (i * angleStep) - (PI / 2.0)
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                drawCircle(color = CyberCyan, radius = 3.5f, center = Offset(x, y))
            }
        }
    }
}
