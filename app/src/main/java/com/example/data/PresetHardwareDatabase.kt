package com.example.data

import com.example.model.HardwareTier

object PresetHardwareDatabase {

    val hardwareTiers = listOf(
        HardwareTier(
            id = "tier_s_plus_2026",
            name = "Flagship Ultra 2026",
            category = "Mobile Flagship",
            chipset = "Snapdragon 8 Elite / Dimensity 9400",
            overallScore = 14200,
            cpuScore = 4800,
            gpuScore = 5200,
            ramScore = 4100,
            diskScore = 3900,
            tierBadge = "S+",
            description = "3nm Octa-Core Oryon/Cortex-X925, Adreno 830 / Immortalis G925, LPDDR5X (10.7 Gbps), UFS 4.0"
        ),
        HardwareTier(
            id = "tier_s_2025",
            name = "High-End Flagship 2024-2025",
            category = "Mobile Flagship",
            chipset = "Snapdragon 8 Gen 3 / Apple A17 Pro",
            overallScore = 11800,
            cpuScore = 3900,
            gpuScore = 4250,
            ramScore = 3450,
            diskScore = 3300,
            tierBadge = "S",
            description = "4nm TSMC 1+5+2 architecture, Adreno 750 / 6-Core GPU, LPDDR5X, UFS 4.0 Storage"
        ),
        HardwareTier(
            id = "tier_a_mid_plus",
            name = "Upper Mid-Range Performance",
            category = "Mobile Mid-Range",
            chipset = "Snapdragon 7+ Gen 3 / Dimensity 8300",
            overallScore = 8600,
            cpuScore = 2950,
            gpuScore = 3100,
            ramScore = 2650,
            diskScore = 2500,
            tierBadge = "A",
            description = "4nm architecture, Cortex-X4 prime core, LPDDR5X memory, UFS 3.1 storage"
        ),
        HardwareTier(
            id = "tier_b_mainstream",
            name = "Mainstream Mid-Range",
            category = "Mobile Mainstream",
            chipset = "Snapdragon 7s Gen 2 / Dimensity 7050",
            overallScore = 5600,
            cpuScore = 1950,
            gpuScore = 1850,
            ramScore = 1800,
            diskScore = 1750,
            tierBadge = "B",
            description = "4nm/6nm Octa-Core (4+4), Mali-G68 / Adreno 710, LPDDR4X/LPDDR5, UFS 2.2"
        ),
        HardwareTier(
            id = "tier_c_budget",
            name = "Entry / Budget Tier",
            category = "Mobile Entry",
            chipset = "MediaTek Helio G99 / Snapdragon 680",
            overallScore = 3200,
            cpuScore = 1150,
            gpuScore = 950,
            ramScore = 1100,
            diskScore = 1050,
            tierBadge = "C",
            description = "6nm Dual-cluster (2+6 Cortex-A76/A55), Mali-G57 MC2, LPDDR4X, eMMC 5.1 / UFS 2.1"
        ),
        HardwareTier(
            id = "tier_pc_desktop",
            name = "Desktop PC Reference Platform",
            category = "Desktop Platform",
            chipset = "AMD Ryzen 7 7800X3D / RTX 4070 Super",
            overallScore = 24500,
            cpuScore = 8200,
            gpuScore = 9400,
            ramScore = 6800,
            diskScore = 7200,
            tierBadge = "PC",
            description = "Desktop 8-Core 16-Thread 5.0GHz, PCIe Gen4 NVMe (7300 MB/s), Dual-Channel DDR5-6000"
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
