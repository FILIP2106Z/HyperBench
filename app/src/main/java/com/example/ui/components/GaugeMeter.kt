package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GaugeMeter(
    value: Float,
    maxValue: Float,
    size: Dp = 200.dp,
    title: String = "SCORE",
    unit: String = "pts",
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (value / maxValue.coerceAtLeast(1f)).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "gauge_progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val diameter = this.size.minDimension - strokeWidth
            val radius = diameter / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            val startAngle = 140f
            val sweepAngleTotal = 260f

            // Background Track Arc
            drawArc(
                color = DarkSurfaceVariant,
                startAngle = startAngle,
                sweepAngle = sweepAngleTotal,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Arc with Gradient
            val activeSweep = sweepAngleTotal * animatedProgress
            if (activeSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyberCyan, ElectricPurple, Color(0xFFFF9100)),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Needle indicator dot
            val currentAngleRad = (startAngle + activeSweep) * (PI / 180.0)
            val needleX = center.x + radius * cos(currentAngleRad).toFloat()
            val needleY = center.y + radius * sin(currentAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.45f,
                center = Offset(needleX, needleY)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.2.sp
            )
            Text(
                text = value.toInt().toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Text(
                text = unit,
                fontSize = 12.sp,
                color = CyberCyan,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
