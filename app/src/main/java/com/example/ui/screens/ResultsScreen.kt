package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BenchmarkRun
import com.example.ui.AppScreen
import com.example.ui.components.ScoreCard
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
fun ResultsScreen(
    run: BenchmarkRun?,
    exportJsonString: String?,
    onNavigate: (AppScreen) -> Unit,
    onRunAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showJsonDialog by remember { mutableStateOf(false) }

    if (run == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No benchmark result available.", color = TextMuted)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigate(AppScreen.HOME) },
                    modifier = Modifier.testTag("results_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "BENCHMARK RESULTS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.2.sp
                )

                IconButton(
                    onClick = {
                        if (exportJsonString != null) {
                            clipboardManager.setText(AnnotatedString(exportJsonString))
                            Toast.makeText(context, "Benchmark JSON profile copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("export_json_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export JSON",
                        tint = CyberCyan
                    )
                }
            }
        }

        // Hero Score Card
        item {
            ScoreCard(run = run)
        }

        // Thermal Contention Banner
        if (run.thermalThrottlingDetected) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DangerRed.copy(alpha = 0.15f))
                        .border(1.dp, DangerRed, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Throttling",
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Thermal / Contention Throttling Detected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                        Text(
                            text = "Workload efficiency decreased under simultaneous multi-core and bus stress.",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // CPU Metric Breakdown
        run.cpuResult?.let { cpu ->
            item {
                MetricDetailCard(
                    title = "CPU PERFORMANCE DETAILS",
                    score = cpu.totalCpuScore,
                    rows = listOf(
                        "Single-Core Score" to "${cpu.singleCoreScore} pts",
                        "Multi-Core Score" to "${cpu.multiCoreScore} pts",
                        "Single-Core Speed" to "${cpu.singleCoreMops} Mops/s",
                        "Multi-Core Speed" to "${cpu.multiCoreMops} Mops/s",
                        "Parallel Cores" to "${cpu.coreCount} Cores",
                        "Scaling Efficiency" to "${(cpu.scalingEfficiency * 100).toInt()}%",
                        "Matrix Linear Algebra" to "${cpu.matrixGflops} GFLOPS",
                        "SHA-256 Throughput" to "${cpu.cryptoHashMbps} MB/s"
                    ),
                    accentColor = CyberCyan
                )
            }
        }

        // GPU & Frame Timing Metric Breakdown
        run.gpuResult?.let { gpu ->
            item {
                MetricDetailCard(
                    title = "GPU & FRAME TIMING ANALYSIS",
                    score = gpu.gpuScore,
                    rows = listOf(
                        "Average Frame Rate" to "${gpu.avgFps} FPS",
                        "1% Low Frame Rate" to "${gpu.low1Fps} FPS",
                        "0.1% Low Frame Rate" to "${gpu.low01Fps} FPS",
                        "Frame Stability Index" to "${gpu.frameStabilityPercent}%",
                        "Particle Compute Count" to "${gpu.particleCount} particles",
                        "Total Frames Rendered" to "${gpu.totalFramesRendered} frames"
                    ),
                    accentColor = ElectricPurple
                )
            }
        }

        // RAM Bandwidth Metric Breakdown
        run.ramResult?.let { ram ->
            item {
                MetricDetailCard(
                    title = "RAM BANDWIDTH & LATENCY",
                    score = ram.ramScore,
                    rows = listOf(
                        "Sequential Read Bandwidth" to "${ram.readBandwidthGbps} GB/s",
                        "Sequential Write Bandwidth" to "${ram.writeBandwidthGbps} GB/s",
                        "Random Access Latency" to "${ram.accessLatencyNs} ns",
                        "Random Throughput" to "${ram.randomThroughputGbps} GB/s"
                    ),
                    accentColor = Color(0xFFFF9100)
                )
            }
        }

        // Storage I/O Metric Breakdown
        run.diskResult?.let { disk ->
            item {
                MetricDetailCard(
                    title = "STORAGE I/O PERFORMANCE",
                    score = disk.diskScore,
                    rows = listOf(
                        "Sequential Write Speed" to "${disk.seqWriteMbps} MB/s",
                        "Sequential Read Speed" to "${disk.seqReadMbps} MB/s",
                        "Random 4K Write IOPS" to "${disk.randomWriteIops} IOPS",
                        "Random 4K Read IOPS" to "${disk.randomReadIops} IOPS"
                    ),
                    accentColor = MatrixGreen
                )
            }
        }

        // Compare Action Button
        item {
            Button(
                onClick = { onNavigate(AppScreen.COMPARE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("compare_tier_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricPurple,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.CompareArrows, contentDescription = "Compare")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "COMPARE WITH HARDWARE TIERS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Secondary Actions (Run Again / Export)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRunAgain,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("run_again_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = CyberCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Run Again", color = CyberCyan)
                }

                OutlinedButton(
                    onClick = {
                        if (exportJsonString != null) {
                            clipboardManager.setText(AnnotatedString(exportJsonString))
                            Toast.makeText(context, "Exported JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_json_profile_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "JSON", tint = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Export JSON", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun MetricDetailCard(
    title: String,
    score: Int,
    rows: List<Pair<String, String>>,
    accentColor: Color
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
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "%,d pts".format(score),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            rows.forEachIndexed { index, (label, value) ->
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
                if (index < rows.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}
