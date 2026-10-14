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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    privilegedDetails: Map<String, String> = emptyMap(),
    privilegedActionMessage: String? = null,
    onSelectBackend: (GpuBackend) -> Unit,
    onSelectDriver: (DriverProfile) -> Unit,
    onRequestRoot: () -> Unit = {},
    onOpenShizuku: () -> Unit = {},
    onRefreshPrivileged: () -> Unit = {},
    onTogglePrivilegedOverride: (String?) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    var overrideThrottling by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRefreshPrivileged()
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
                    modifier = Modifier.testTag("gpu_settings_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Text(
                    text = "GPU PIPELINE & PRIVILEGED CONTROL",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.1.sp
                )

                IconButton(
                    onClick = { onRefreshPrivileged() },
                    modifier = Modifier.testTag("refresh_privileged_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberCyan)
                }
            }
        }

        // Action Feedback Banner
        privilegedActionMessage?.let { msg ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 1. Real Root & Shizuku Privileged Hardware Control
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
                                tint = if (liveTelemetry.privilegedAccess.contains("Root") || liveTelemetry.privilegedAccess.contains("Shizuku")) MatrixGreen else CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PRIVILEGED CONTROL & ACCESS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (liveTelemetry.privilegedAccess.contains("Root") || liveTelemetry.privilegedAccess.contains("Shizuku")) MatrixGreen.copy(alpha = 0.18f) else DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = liveTelemetry.privilegedAccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (liveTelemetry.privilegedAccess.contains("Root") || liveTelemetry.privilegedAccess.contains("Shizuku")) MatrixGreen else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Root (Magisk / KernelSU / APatch) and Shizuku ADB unlock direct access to hardware thermal zones (/sys/class/thermal), raw Adreno GPU clock governors (/sys/class/kgsl), and disabling thermal throttle limits for unconstrained sustained testing.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real Root & Shizuku Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRequestRoot,
                            modifier = Modifier.weight(1f).testTag("request_root_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (liveTelemetry.privilegedAccess.contains("Root Superuser")) MatrixGreen else DarkSurfaceVariant,
                                contentColor = if (liveTelemetry.privilegedAccess.contains("Root Superuser")) Color(0xFF021B0F) else TextPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LockOpen, contentDescription = "Root", modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (liveTelemetry.privilegedAccess.contains("Root Superuser")) "Root Active" else "Grant Root (SU)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenShizuku,
                            modifier = Modifier.weight(1f).testTag("open_shizuku_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CyberCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Shizuku", modifier = Modifier.size(15.dp), tint = CyberCyan)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Shizuku ADB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            }
                        }
                    }

                    // Privileged hardware metrics if available
                    if (privilegedDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "LIVE PRIVILEGED HARDWARE REGISTERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MatrixGreen)
                                privilegedDetails.forEach { (k, v) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = k, fontSize = 11.sp, color = TextSecondary)
                                        Text(text = v, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Override Thermal Throttling Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bypass Thermal Frequency Caps",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Forces performance governor during stress",
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

                    // Manual simulation fallback buttons
                    Text(text = "Manual Override (For Testing / Emulators):", fontSize = 10.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
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
                            isSelected = liveTelemetry.privilegedAccess.contains("Shizuku"),
                            onClick = { onTogglePrivilegedOverride("Shizuku ADB") }
                        )
                        PrivilegedModeButton(
                            title = "Root SU",
                            isSelected = liveTelemetry.privilegedAccess.contains("Root Superuser"),
                            onClick = { onTogglePrivilegedOverride("Root Superuser") }
                        )
                    }
                }
            }
        }

        // 2. GPU Backend Selection
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

        // 3. GPU Driver Profiles
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
