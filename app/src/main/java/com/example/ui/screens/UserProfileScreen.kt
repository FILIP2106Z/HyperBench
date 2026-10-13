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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.BenchmarkRun
import com.example.model.UserProfile
import com.example.ui.AppScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileScreen(
    currentProfile: UserProfile,
    allProfiles: List<UserProfile>,
    userRuns: List<BenchmarkRun>,
    onSwitchProfile: (UserProfile) -> Unit,
    onCreateProfile: (username: String, displayName: String, deviceAlias: String, colorHex: String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.HOME)
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    val myRuns = remember(userRuns, currentProfile) {
        userRuns.filter { it.profileId == currentProfile.id }
    }

    val bestOverall = myRuns.maxOfOrNull { it.overallScore } ?: 0
    val bestCpu = myRuns.mapNotNull { it.cpuResult?.totalCpuScore }.maxOrNull() ?: 0
    val bestGpu = myRuns.mapNotNull { it.gpuResult?.gpuScore }.maxOrNull() ?: 0
    val bestRam = myRuns.mapNotNull { it.ramResult?.ramScore }.maxOrNull() ?: 0
    val bestDisk = myRuns.mapNotNull { it.diskResult?.diskScore }.maxOrNull() ?: 0
    val avgScore = if (myRuns.isNotEmpty()) (myRuns.map { it.overallScore }.average()).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onNavigate(AppScreen.HOME) },
                    modifier = Modifier.testTag("profile_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }

                Text(
                    text = "USER ACCOUNT & PROFILES",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.2.sp
                )

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.testTag("add_profile_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile", tint = CyberCyan)
                }
            }
        }

        // Active Profile Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .border(2.dp, CyberCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = CyberCyan,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = currentProfile.displayName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "@${currentProfile.username} · ${currentProfile.deviceAlias}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "Account created ${SimpleDateFormat("MMM yyyy", Locale.US).format(Date(currentProfile.createdAt))}",
                                fontSize = 10.sp,
                                color = CyberCyan
                            )
                        }
                    }
                }
            }
        }

        // Account Switcher List
        item {
            Text(
                text = "SWITCH ACCOUNT / HARDWARE PROFILE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(allProfiles) { profile ->
                    val isSelected = profile.id == currentProfile.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.18f) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) CyberCyan else DarkSurfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSwitchProfile(profile) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = profile.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CyberCyan else TextPrimary
                            )
                            Text(
                                text = profile.deviceAlias,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Performance Trends Overview
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
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Trends",
                                tint = MatrixGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PERSONAL HARDWARE PERFORMANCE TRENDS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${myRuns.size} Tests",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileStatTile("BEST SCORE", if (bestOverall > 0) "%,d".format(bestOverall) else "—", CyberCyan)
                        ProfileStatTile("AVG SCORE", if (avgScore > 0) "%,d".format(avgScore) else "—", ElectricPurple)
                        ProfileStatTile("BEST CPU", if (bestCpu > 0) "%,d".format(bestCpu) else "—", Color(0xFFFF9100))
                        ProfileStatTile("BEST GPU", if (bestGpu > 0) "%,d".format(bestGpu) else "—", MatrixGreen)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sparkline or recent history points
                    if (myRuns.isNotEmpty()) {
                        Text(
                            text = "Recent Test Run Progression",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val recent = myRuns.take(12).reversed()
                            val max = (recent.maxOfOrNull { it.overallScore } ?: 1).toFloat()
                            recent.forEach { run ->
                                val heightFrac = (run.overallScore / max).coerceIn(0.15f, 1f)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp * heightFrac)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CyberCyan)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to create new account / profile
    if (showCreateDialog) {
        CreateProfileDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { user, disp, alias, color ->
                onCreateProfile(user, disp, alias, color)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ProfileStatTile(title: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = accent
        )
    }
}

@Composable
private fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (username: String, displayName: String, deviceAlias: String, colorHex: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var deviceAlias by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Hardware Profile", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("e.g. Overclocked Rig") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedLabelColor = CyberCyan
                    )
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("e.g. tech_runner") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedLabelColor = CyberCyan
                    )
                )
                OutlinedTextField(
                    value = deviceAlias,
                    onValueChange = { deviceAlias = it },
                    label = { Text("Hardware Alias") },
                    placeholder = { Text("e.g. Test Bed #1") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedLabelColor = CyberCyan
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(username, displayName, deviceAlias, "#00E5FF") },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF041017))
            ) {
                Text("Create Account", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}
