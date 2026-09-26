package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.model.SpeedUnit
import com.example.ui.components.NeumorphicCard
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed

@Composable
fun SettingsScreen(
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val settingsRepo = app.settingsRepository
    val tripRepo = app.tripRepository
    val settings by settingsRepo.settings.collectAsStateWithLifecycle()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var backendUrlInput by remember { mutableStateOf(settings.aiBackendUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Preferences, speed limits, units & AI backend",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Section: Speed & Units
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "SPEED & UNITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                // Unit Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Unit of Speed", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(SpeedUnit.KMH, SpeedUnit.MPH).forEach { u ->
                            val isSel = settings.speedUnit == u
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) CyanNeon else Color(0xFF1E293B))
                                    .clickable { settingsRepo.updateSpeedUnit(u) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = u.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF0F172A) else Color.White
                                )
                            }
                        }
                    }
                }

                // Speed Limit Selector
                Text("Default Speed Warning Limit", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(40.0, 50.0, 60.0, 80.0, 100.0, 120.0).forEach { lim ->
                        val isSel = settings.speedLimitKmh == lim
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) CyanNeon else Color(0xFF1E293B))
                                .clickable { settingsRepo.updateSpeedLimit(lim) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${lim.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }
            }
        }

        // Section: Alerts & Haptics
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ALERTS & HAPTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Vibration Alert", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Haptic pulses when exceeding speed limit", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = settings.isVibrationEnabled,
                        onCheckedChange = { settingsRepo.setVibrationEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanNeon)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Audio Alert Tone", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Chime upon critical speed warnings", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Switch(
                        checked = settings.isSoundAlertEnabled,
                        onCheckedChange = { settingsRepo.setSoundEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanNeon)
                    )
                }
            }
        }

        // Section: AI Backend Configuration (Section 19 Secure config)
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "WAFA AI BACKEND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "Provide your custom private AI proxy URL. Secrets and API keys are stored on the server-side, never bundled in the APK.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = backendUrlInput,
                    onValueChange = {
                        backendUrlInput = it
                        settingsRepo.setAiBackendUrl(it)
                    },
                    label = { Text("Private AI Endpoint (e.g. https://api.example.com/v1)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section: Permissions & Privacy
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PRIVACY & STORAGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                Button(
                    onClick = onNavigateToPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Inspect System Permissions", color = Color.White)
                }

                Button(
                    onClick = { showClearHistoryDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f))
                ) {
                    Text("Delete All Trip History", color = Color.White)
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Delete All Trip History?") },
            text = { Text("This will permanently purge all recorded trips, route coordinates, and speed telemetry. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        kotlinx.coroutines.runBlocking { tripRepo.deleteAllTrips() }
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Purge Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
