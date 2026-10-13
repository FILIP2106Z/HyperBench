package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MultiPlatformDocsScreen(
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    modifier = Modifier.testTag("docs_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Text(
                    text = "MULTI-PLATFORM ARCHITECTURE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Introduction Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CROSS-PLATFORM BENCHMARK ENGINE SPECIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This engineering specification outlines the adaptations required to deploy the HyperBench CPU, GPU, RAM, and Disk testing suites across Windows, macOS, and Linux on x86_64 and ARM64 architectures.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 1. CPU Architecture
        item {
            PlatformDocCard(
                section = "1. CPU COMPUTE & SCHEDULING (x86_64 VS ARM64)",
                accentColor = CyberCyan,
                challenges = "Disparate SIMD vector widths (AVX2 256-bit / AVX-512 vs ARM NEON 128-bit / SVE scalable vectors) and hybrid big.LITTLE / P-core vs E-core scheduling variances across operating systems.",
                solutions = listOf(
                    "Windows: Bind high-throughput matrix worker threads using SetThreadIdealProcessorEx() to avoid scheduling heavy compute onto efficiency cores.",
                    "Linux: Query sysfs (/sys/devices/system/cpu/cpu*/topology) and enforce sched_setaffinity(2) to bind threads to physical performance clusters.",
                    "macOS: Dispatch worker threads via Grand Central Dispatch with QOS_CLASS_USER_INTERACTIVE to guarantee execution on Apple Silicon Firestorm/Avalanche cores.",
                    "Vector Math: Utilize Highway (Google's C++ library) or standard intrinsics with runtime CPU feature dispatch to generate optimal vector code per architecture."
                )
            )
        }

        // 2. GPU Rendering
        item {
            PlatformDocCard(
                section = "2. GPU RENDERING & FRAME TIMING (D3D12 / METAL / VULKAN)",
                accentColor = ElectricPurple,
                challenges = "API fragmentation across ecosystems: Direct3D 12 on Windows, Metal 3 on macOS, and Vulkan on Linux/Android. Additionally, desktop GPUs use discrete VRAM with PCIe bus transfers, whereas mobile and Apple Silicon use Unified Memory.",
                solutions = listOf(
                    "Unified Shader Layer: Adopt WebGPU (via Google Dawn or Rust wgpu-native) or spirv-cross to compile HLSL/GLSL particle compute shaders into Metal Shading Language (MSL), SPIR-V, and DXIL.",
                    "macOS (Metal 3): Leverage MTLCommandBuffer GPU completion handlers with CAMetalLayer to record microsecond presentation latency without display server compositor interference.",
                    "Windows (Direct3D 12): Query IDXGISwapChain::GetFrameStatistics to capture GPU flip queue times, dropped frames, and true 1% low frame time percentiles.",
                    "Linux (Vulkan/DRM): Use VK_EXT_present_timing to measure the exact millisecond timestamps when scanout hardware scans scanlines to the physical monitor."
                )
            )
        }

        // 3. RAM Bandwidth
        item {
            PlatformDocCard(
                section = "3. RAM MEMORY BANDWIDTH & CACHE SUBSYSTEM",
                accentColor = Color(0xFFFF9100),
                challenges = "OS paging policies, Transparent Huge Pages, and NUMA node configurations causing non-uniform memory access latencies across multi-socket and multi-channel platforms.",
                solutions = listOf(
                    "Windows: Allocate memory blocks via VirtualAlloc() with MEM_LARGE_PAGES and query NUMA node affinity using VirtualAllocExNuma().",
                    "Linux: Allocate buffers via mmap() with MAP_HUGETLB and madvise(MADV_HUGEPAGE) to eliminate Translation Lookaside Buffer (TLB) misses during streaming bandwidth passes.",
                    "macOS: Utilize vm_allocate() with VM_FLAGS_PURGABLE. On Apple Silicon unified memory (UMA), coordinate with GPU benchmarks to measure concurrent CPU-GPU memory bus contention.",
                    "Bandwidth Timing: Employ clock_gettime(CLOCK_MONOTONIC_RAW) on POSIX and QueryPerformanceCounter() on Windows for sub-nanosecond access latency precision."
                )
            )
        }

        // 4. Disk & Storage I/O
        item {
            PlatformDocCard(
                section = "4. STORAGE I/O & FILE SYSTEM SUBSYSTEM",
                accentColor = MatrixGreen,
                challenges = "OS page cache caching reads/writes in RAM rather than stressing actual NVMe/UFS flash NAND blocks, creating misleading benchmark spikes.",
                solutions = listOf(
                    "Windows: Open test files with CreateFile(FILE_FLAG_NO_BUFFERING | FILE_FLAG_WRITE_THROUGH) to bypass the Windows System Cache directly into NVMe controller queues.",
                    "Linux: Deploy modern io_uring asynchronous ring buffers with O_DIRECT flags to measure raw NVMe 4K random IOPS without kernel context-switch bottlenecks.",
                    "macOS: Invoke fcntl(fd, F_NOCACHE, 1) on APFS storage volumes to disable the macOS Unified Buffer Cache and benchmark true SSD flash block latency."
                )
            )
        }
    }
}

@Composable
private fun PlatformDocCard(
    section: String,
    accentColor: Color,
    challenges: String,
    solutions: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Key Challenges:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = challenges,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Engineering Solutions & Native APIs:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            solutions.forEach { solution ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = solution,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
