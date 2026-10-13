package com.example.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetHardwareDatabase
import com.example.model.BenchmarkRun
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierA
import com.example.ui.theme.TierB
import com.example.ui.theme.TierC
import com.example.ui.theme.TierD
import com.example.ui.theme.TierS

@Composable
fun ScoreCard(
    run: BenchmarkRun,
    modifier: Modifier = Modifier
) {
    val (tierBadge, tierLabel) = PresetHardwareDatabase.getTierForScore(run.overallScore)
    val badgeColor = when (tierBadge) {
        "S+", "S" -> TierS
        "A" -> TierA
        "B" -> TierB
        "C" -> TierC
        else -> TierD
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = run.type.title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${run.deviceManufacturer} ${run.deviceModel}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            // Tier Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f))
                    .border(1.5.dp, badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tierBadge,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Points Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "HARDWARE RATING",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tierLabel,
                    fontSize = 13.sp,
                    color = badgeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "%,d".format(run.overallScore),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "pts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Breakdown items
        SubsystemBar(
            title = "CPU Compute",
            score = run.cpuResult?.totalCpuScore ?: (run.overallScore.takeIf { run.type.name.contains("CPU") } ?: 0),
            maxScore = 5000,
            color = CyberCyan
        )
        Spacer(modifier = Modifier.height(6.dp))
        SubsystemBar(
            title = "GPU & Frames",
            score = run.gpuResult?.gpuScore ?: (run.overallScore.takeIf { run.type.name.contains("GPU") } ?: 0),
            maxScore = 5500,
            color = ElectricPurple
        )
        Spacer(modifier = Modifier.height(6.dp))
        SubsystemBar(
            title = "RAM Bandwidth",
            score = run.ramResult?.ramScore ?: (run.overallScore.takeIf { run.type.name.contains("RAM") } ?: 0),
            maxScore = 4500,
            color = Color(0xFFFF9100)
        )
        Spacer(modifier = Modifier.height(6.dp))
        SubsystemBar(
            title = "Storage I/O",
            score = run.diskResult?.diskScore ?: (run.overallScore.takeIf { run.type.name.contains("DISK") } ?: 0),
            maxScore = 4000,
            color = Color(0xFF00E676)
        )
    }
}

@Composable
fun SubsystemBar(
    title: String,
    score: Int,
    maxScore: Int,
    color: Color
) {
    val progress = (score.toFloat() / maxScore).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = if (score > 0) "%,d pts".format(score) else "—",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SurfaceCard
        )
    }
}
