package com.example.data

import com.example.model.HardwareTier

object PresetHardwareDatabase {

    val hardwareTiers = listOf(
        HardwareTier(
            id = "tier_s_plus_2026",
            name = "Flagship Ultra 2026",
            category = "Mobile Flagship",
            chipset = "Snapdragon 8 Elite / Dimensity 9400",
            overallScore = 15200,
            cpuScore = 4800,
            gpuScore = 5200,
            ramScore = 4100,
            diskScore = 3900,
            tierBadge = "S+",
            description = "3nm Octa-Core Oryon/Cortex-X925 (4.32 GHz), Adreno 830 GPU, LPDDR5X (10.7 Gbps), UFS 4.0 4-lane (~4200 MB/s)"
        ),
        HardwareTier(
            id = "tier_s_2025",
            name = "High-End Flagship",
            category = "Mobile Flagship",
            chipset = "Snapdragon 8 Gen 3 / Apple A17 Pro",
            overallScore = 12500,
            cpuScore = 3900,
            gpuScore = 4250,
            ramScore = 3450,
            diskScore = 3300,
            tierBadge = "S",
            description = "4nm TSMC 1+5+2 architecture (Cortex-X4 @ 3.3GHz), Adreno 750 GPU, LPDDR5X (8533 Mbps), UFS 4.0 (~3800 MB/s)"
        ),
        HardwareTier(
            id = "tier_a_mid_plus",
            name = "Upper Mid-Range",
            category = "Mobile Mid-Range",
            chipset = "Snapdragon 7+ Gen 3 / Dimensity 8300",
            overallScore = 9400,
            cpuScore = 2950,
            gpuScore = 3100,
            ramScore = 2650,
            diskScore = 2500,
            tierBadge = "A",
            description = "4nm architecture, Cortex-X4 prime core, Adreno 732 GPU, LPDDR5X memory, UFS 3.1 storage (~2000 MB/s)"
        ),
        HardwareTier(
            id = "tier_b_mainstream",
            name = "Mainstream Mid-Range",
            category = "Mobile Mainstream",
            chipset = "Snapdragon 7s Gen 2 / Dimensity 7050",
            overallScore = 6100,
            cpuScore = 1950,
            gpuScore = 1850,
            ramScore = 1800,
            diskScore = 1750,
            tierBadge = "B",
            description = "4nm/6nm Octa-Core (4+4 Cortex-A78/A55), Adreno 710 / Mali-G68, LPDDR4X/LPDDR5, UFS 2.2 storage (~950 MB/s)"
        ),
        HardwareTier(
            id = "tier_c_budget",
            name = "Budget Snapdragon 680 / eMMC",
            category = "Mobile Entry",
            chipset = "Snapdragon 680 / Adreno 610 / eMMC 5.1",
            overallScore = 3500,
            cpuScore = 1150,
            gpuScore = 950,
            ramScore = 1100,
            diskScore = 1050,
            tierBadge = "C",
            description = "6nm Qualcomm Snapdragon 680 (4x Cortex-A73 @ 2.4GHz + 4x Cortex-A53 @ 1.9GHz), Adreno 610 GPU, LPDDR4X (2133 MHz, ~14 GB/s), eMMC 5.1 storage (~240 MB/s R, ~110 MB/s W)"
        ),
        HardwareTier(
            id = "tier_pc_desktop",
            name = "Desktop PC Reference Platform",
            category = "Desktop Platform",
            chipset = "AMD Ryzen 7 7800X3D / RTX 4070 Super",
            overallScore = 26500,
            cpuScore = 8200,
            gpuScore = 9400,
            ramScore = 6800,
            diskScore = 7200,
            tierBadge = "PC",
            description = "Desktop 8-Core 16-Thread 5.0GHz, RTX 4070 Super (36 TFLOPS), PCIe Gen4 NVMe (7300 MB/s, 250k IOPS), Dual-Channel DDR5-6000 (~85 GB/s)"
        )
    )

    fun getTierForScore(score: Int): Pair<String, String> {
        return when {
            score >= 13500 -> "S+" to "Godlike Mobile Tier"
            score >= 10500 -> "S" to "Elite Flagship Tier"
            score >= 7500 -> "A" to "Upper Mid-Range Tier"
            score >= 4800 -> "B" to "Mainstream Mid-Tier"
            score >= 2500 -> "C" to "Standard Entry Tier"
            else -> "D" to "Basic Low-Power Tier"
        }
    }
}
