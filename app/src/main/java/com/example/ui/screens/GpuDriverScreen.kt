package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DriverProfile
import com.example.model.GpuBackend
import com.example.model.LiveTelemetry
import com.example.ui.AppScreen
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
fun GpuDriverScreen(
    currentBackend: GpuBackend,
    currentDriver: DriverProfile,
    liveTelemetry: LiveTelemetry,
    onSelectBackend: (GpuBackend) -> Unit,
    onSelectDriver: (DriverProfile) -> Unit,
    onTogglePrivilegedOverride: (String?) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    var overrideThrottling by remember { mutableStateOf(false) }

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
                    modifier = Modifier.testTag("gpu_settings_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Text(
                    text = "GPU BACKEND & DRIVER PIPELINE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // 1. GPU Backend Selection
        item {
            Text(
                text = "GRAPHICS RENDERING BACKEND",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GpuBackend.entries.forEach { backend ->
                    val isSelected = backend == currentBackend
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.15f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) CyberCyan else DarkSurfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectBackend(backend) }
                            .padding(14.dp)
                            .testTag("backend_${backend.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = backend.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyberCyan else TextPrimary
                                )
                                Text(
                                    text = backend.versionTag,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. GPU Driver Profiles (e.g. Turnip on Adreno)
        item {
            Text(
                text = "GPU DRIVER LAYER (TURNIP / ADRENO / SYSTEM)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricPurple,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DriverProfile.entries.forEach { driver ->
                    val isSelected = driver == currentDriver
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricPurple.copy(alpha = 0.15f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) ElectricPurple else DarkSurfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectDriver(driver) }
                            .padding(14.dp)
                            .testTag("driver_${driver.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = driver.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ElectricPurple else TextPrimary
                                )
                                Text(
                                    text = driver.description,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ElectricPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Shizuku & Root Enhanced Experience
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Privileged",
                                tint = MatrixGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRIVILEGED HARDWARE CONTROL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MatrixGreen.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = liveTelemetry.privilegedAccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Shizuku and Root superuser unlock access to hardware thermal zones (/sys/class/thermal), raw Adreno/Mali GPU frequency governors (/sys/class/kgsl), and disabling thermal throttling limits for sustained maximum throughput.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Override Thermal Throttle Limits",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Bypass OEM background frequency caps",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = overrideThrottling,
                            onCheckedChange = {
                                overrideThrottling = it
                                onTogglePrivilegedOverride(if (it) "Root Superuser" else null)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MatrixGreen,
                                checkedTrackColor = MatrixGreen.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrivilegedModeButton(
                            title = "Standard",
                            isSelected = liveTelemetry.privilegedAccess == "Standard User",
                            onClick = { onTogglePrivilegedOverride(null) }
                        )
                        PrivilegedModeButton(
                            title = "Shizuku ADB",
                            isSelected = liveTelemetry.privilegedAccess == "Shizuku ADB",
                            onClick = { onTogglePrivilegedOverride("Shizuku ADB") }
                        )
                        PrivilegedModeButton(
                            title = "Root SU",
                            isSelected = liveTelemetry.privilegedAccess == "Root Superuser",
                            onClick = { onTogglePrivilegedOverride("Root Superuser") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivilegedModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MatrixGreen.copy(alpha = 0.2f) else SurfaceCard)
            .border(1.dp, if (isSelected) MatrixGreen else DarkSurfaceVariant, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MatrixGreen else TextSecondary
        )
    }
}
