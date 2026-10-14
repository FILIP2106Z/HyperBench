package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.benchmark.GpuBenchmarkEngine
import com.example.model.FrameTimingStats
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    var angle: Float,
    var radius: Float,
    var z: Float,
    var speed: Float,
    var zSpeed: Float,
    var baseColor: Color
)

private class Vertex3D(val x: Float, val y: Float, val z: Float)

@Composable
fun GpuBenchmarkRenderer(
    engine: GpuBenchmarkEngine,
    particleCount: Int = 3000,
    isComplexMode: Boolean = true,
    modifier: Modifier = Modifier,
    onFrameStats: (FrameTimingStats) -> Unit
) {
    // Generate particle population
    val particles = remember(particleCount) {
        val rand = Random(1337)
        val colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFF2979FF),
            Color(0xFF7C4DFF),
            Color(0xFFFF9100),
            Color(0xFF00E676)
        )
        List(particleCount) {
            Particle(
                angle = rand.nextFloat() * (2f * PI.toFloat()),
                radius = rand.nextFloat() * 260f + 30f,
                z = rand.nextFloat() * 600f - 300f,
                speed = rand.nextFloat() * 0.035f + 0.015f,
                zSpeed = rand.nextFloat() * 2.5f - 1.25f,
                baseColor = colors[rand.nextInt(colors.size)]
            )
        }
    }

    // Precalculate 3D Polyhedron vertices (Icosahedron base)
    val meshVertices = remember {
        val phi = (1.0 + Math.sqrt(5.0)).toFloat() / 2.0f
        val scale = 110f
        listOf(
            Vertex3D(-scale, phi * scale, 0f),
            Vertex3D(scale, phi * scale, 0f),
            Vertex3D(-scale, -phi * scale, 0f),
            Vertex3D(scale, -phi * scale, 0f),
            Vertex3D(0f, -scale, phi * scale),
            Vertex3D(0f, scale, phi * scale),
            Vertex3D(0f, -scale, -phi * scale),
            Vertex3D(0f, scale, -phi * scale),
            Vertex3D(phi * scale, 0f, -scale),
            Vertex3D(phi * scale, 0f, scale),
            Vertex3D(-phi * scale, 0f, -scale),
            Vertex3D(-phi * scale, 0f, scale)
        )
    }

    val meshEdges = remember {
        listOf(
            0 to 11, 0 to 5, 0 to 1, 0 to 7, 0 to 10,
            1 to 5, 1 to 9, 1 to 8, 1 to 7,
            2 to 11, 2 to 4, 2 to 3, 2 to 6, 2 to 10,
            3 to 4, 3 to 9, 3 to 8, 3 to 6,
            4 to 5, 4 to 9, 4 to 11,
            5 to 9, 5 to 11,
            6 to 7, 6 to 8, 6 to 10,
            7 to 8, 7 to 10,
            8 to 9, 10 to 11
        )
    }

    var frameTick by remember { mutableIntStateOf(0) }
    var rotationAngleX by remember { mutableStateOf(0f) }
    var rotationAngleY by remember { mutableStateOf(0f) }

    var lastExecutionNanos by remember { mutableStateOf(0L) }

    // Driving high-refresh animation loop
    LaunchedEffect(engine, particleCount) {
        while (true) {
            withFrameNanos { nowNanos ->
                val stats = engine.recordFrame(nowNanos, lastExecutionNanos)
                onFrameStats(stats)
                rotationAngleX += 0.02f
                rotationAngleY += 0.028f
                frameTick++
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val renderStartNanos = System.nanoTime()
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Draw radial background glow
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF131D33), Color(0xFF080C14)),
                center = Offset(cx, cy),
                radius = maxOf(cx, cy) * 1.1f
            )
        )

        // Draw perspective grid floor
        val gridY = cy + 180f
        val gridLines = 14
        for (i in -gridLines..gridLines) {
            val startX = cx + (i * 28f)
            val endX = cx + (i * 120f)
            drawLine(
                color = Color(0x1800E5FF),
                start = Offset(startX, gridY),
                end = Offset(endX, h),
                strokeWidth = 1.2f
            )
        }
        for (j in 1..6) {
            val yOffset = gridY + (j * j * 7f)
            if (yOffset <= h) {
                drawLine(
                    color = Color(0x2000E5FF),
                    start = Offset(0f, yOffset),
                    end = Offset(w, yOffset),
                    strokeWidth = 1.2f
                )
            }
        }

        // 1. Update and draw 3D Particle Storm
        val particlePoints = ArrayList<Offset>(particleCount)
        val particleColors = ArrayList<Color>(particleCount)

        for (p in particles) {
            p.angle += p.speed
            p.z += p.zSpeed
            if (p.z > 350f) {
                p.z = -350f
            } else if (p.z < -350f) {
                p.z = 350f
            }

            val px = p.radius * cos(p.angle)
            val py = p.radius * sin(p.angle) * 0.45f
            val pz = p.z

            // 3D perspective projection
            val distance = 500f
            val factor = distance / (distance + pz)
            val screenX = cx + px * factor
            val screenY = cy + py * factor

            if (screenX in 0f..w && screenY in 0f..h) {
                val alpha = (factor * 0.9f).coerceIn(0.15f, 1f)
                val dotColor = p.baseColor.copy(alpha = alpha)
                drawCircle(
                    color = dotColor,
                    radius = (factor * 2.2f).coerceIn(1.2f, 5.5f),
                    center = Offset(screenX, screenY)
                )
            }
        }

        // 2. Draw 3D Rotating Polyhedron in the center
        if (isComplexMode) {
            val cosX = cos(rotationAngleX)
            val sinX = sin(rotationAngleX)
            val cosY = cos(rotationAngleY)
            val sinY = sin(rotationAngleY)

            // Project all 12 vertices
            val projectedVertices = meshVertices.map { v ->
                // Rotate around Y
                val x1 = v.x * cosY + v.z * sinY
                val y1 = v.y
                val z1 = -v.x * sinY + v.z * cosY

                // Rotate around X
                val x2 = x1
                val y2 = y1 * cosX - z1 * sinX
                val z2 = y1 * sinX + z1 * cosX

                val camDist = 420f
                val projFactor = camDist / (camDist + z2)
                Offset(cx + x2 * projFactor, cy + y2 * projFactor)
            }

            // Draw edges
            for ((startIdx, endIdx) in meshEdges) {
                val p1 = projectedVertices[startIdx]
                val p2 = projectedVertices[endIdx]
                drawLine(
                    color = Color(0xCC00E5FF),
                    start = p1,
                    end = p2,
                    strokeWidth = 2.4f,
                    cap = StrokeCap.Round
                )
            }

            // Draw glowing vertex nodes
            for (pt in projectedVertices) {
                drawCircle(
                    color = Color(0xFFFF9100),
                    radius = 4.2f,
                    center = pt
                )
            }

            // Center energy ring
            drawCircle(
                color = Color(0x667C4DFF),
                radius = 135f + (sin(rotationAngleX * 2.5f) * 12f),
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f)
            )
        }
        lastExecutionNanos = System.nanoTime() - renderStartNanos
    }
}
