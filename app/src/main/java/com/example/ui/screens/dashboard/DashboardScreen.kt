package com.example.ui.screens.dashboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.model.SpeedUnit
import com.example.data.model.TripStatus
import com.example.data.model.WarningLevel
import com.example.data.service.TripTrackingService
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.SpeedometerGauge
import com.example.ui.components.TripMetricsGrid
import com.example.ui.components.WafaIcons
import com.example.ui.components.WarningBanner
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MintSuccess
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DashboardScreen(
    onNavigateToLiveMap: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToWarningCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val stateMachine = app.tripStateMachine
    val settingsRepo = app.settingsRepository
    val vehicleRepo = app.vehicleRepository

    val tripStatus by stateMachine.status.collectAsStateWithLifecycle()
    val activeMetrics by stateMachine.activeMetrics.collectAsStateWithLifecycle()
    val liveGps by stateMachine.liveGps.collectAsStateWithLifecycle()
    val settings by settingsRepo.settings.collectAsStateWithLifecycle()
    val vehicles by vehicleRepo.allVehicles.collectAsStateWithLifecycle(emptyList())

    val selectedVehicle = vehicles.firstOrNull { it.id == activeMetrics.vehicleId }
        ?: vehicles.firstOrNull { it.isDefault }
        ?: vehicles.firstOrNull()

    var showDiscardConfirmDialog by remember { mutableStateOf(false) }
    var isHudLandscapeMode by remember { mutableStateOf(false) }

    // Haptic vibration feedback for high-speed events
    LaunchedEffect(Unit) {
        stateMachine.speedWarningEvent.collectLatest {
            if (settings.isVibrationEnabled) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.let { v ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(400)
                    }
                }
            }
        }
    }

    if (isHudLandscapeMode) {
        // Landscape HUD full-screen dashboard
        HudLandscapeSpeedometer(
            speed = if (tripStatus == TripStatus.TRACKING) activeMetrics.currentSpeedKmh else liveGps.speedKmh,
            unit = settings.speedUnit,
            speedLimit = settings.speedLimitKmh,
            distanceMeters = activeMetrics.distanceMeters,
            durationSeconds = activeMetrics.durationSeconds,
            isOverSpeed = activeMetrics.isOverSpeed,
            onCloseHud = { isHudLandscapeMode = false }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header: Active Vehicle Pill + HUD Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle Selector Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF131B2F))
                    .border(1.dp, Color(0xFF2A3A60), RoundedCornerShape(14.dp))
                    .clickable(onClick = onNavigateToVehicles)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = WafaIcons.Vehicle,
                        contentDescription = "Selected Vehicle",
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedVehicle?.name ?: "Primary Vehicle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Analog / Digital Gauge Toggle
                IconButton(
                    onClick = { settingsRepo.setAnalogGauge(!settings.isAnalogGauge) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF131B2F))
                ) {
                    Icon(
                        imageVector = WafaIcons.Speedometer,
                        contentDescription = "Toggle Gauge Style",
                        tint = if (settings.isAnalogGauge) AmberSpeed else CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Full HUD Mode
                IconButton(
                    onClick = { isHudLandscapeMode = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF131B2F))
                ) {
                    Icon(
                        imageVector = WafaIcons.Compass,
                        contentDescription = "Open HUD Display",
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Speed / GPS Warnings Banner
        if (activeMetrics.isOverSpeed) {
            WarningBanner(
                title = "SPEED LIMIT EXCEEDED",
                message = "Current speed is above ${settings.speedLimitKmh.toInt()} ${settings.speedUnit.label}. Please drive safely.",
                level = WarningLevel.CRITICAL
            )
        } else if (liveGps.isSignalLost) {
            WarningBanner(
                title = "GPS SIGNAL LOST",
                message = "Waiting for reliable GPS satellite lock...",
                level = WarningLevel.WARNING
            )
        } else if (liveGps.isAccuracyPoor) {
            WarningBanner(
                title = "LOW GPS ACCURACY",
                message = "GPS accuracy is ±${liveGps.accuracy.toInt()}m. Speed readings may fluctuate.",
                level = WarningLevel.INFO
            )
        }

        // Central Speedometer Gauge (Digital or Analog)
        val currentSpeedToDisplay = if (tripStatus == TripStatus.TRACKING) {
            activeMetrics.currentSpeedKmh
        } else {
            liveGps.speedKmh
        }

        SpeedometerGauge(
            currentSpeedKmh = currentSpeedToDisplay,
            speedLimitKmh = settings.speedLimitKmh,
            unit = settings.speedUnit,
            isAnalog = settings.isAnalogGauge,
            isMoving = liveGps.isMoving,
            isOverSpeed = activeMetrics.isOverSpeed,
            onUnitClick = {
                val nextUnit = if (settings.speedUnit == SpeedUnit.KMH) SpeedUnit.MPH else SpeedUnit.KMH
                settingsRepo.updateSpeedUnit(nextUnit)
            }
        )

        // STRICT TRIP CONTROLS: NO START = NO RECORDING!
        TripControlBar(
            status = tripStatus,
            onStartTrip = {
                kotlinx.coroutines.runBlocking {
                    stateMachine.startTrip(
                        vehicleId = selectedVehicle?.id,
                        speedLimitKmh = settings.speedLimitKmh
                    )
                    TripTrackingService.startService(context)
                }
            },
            onPauseTrip = {
                kotlinx.coroutines.runBlocking {
                    stateMachine.pauseTrip()
                }
            },
            onResumeTrip = {
                kotlinx.coroutines.runBlocking {
                    stateMachine.resumeTrip()
                }
            },
            onStopAndSaveTrip = {
                kotlinx.coroutines.runBlocking {
                    stateMachine.stopTripAndSave()
                    TripTrackingService.stopService(context)
                }
            },
            onDiscardTrip = {
                showDiscardConfirmDialog = true
            }
        )

        // Trip Telemetry Metrics
        TripMetricsGrid(
            distanceMeters = if (tripStatus != TripStatus.IDLE) activeMetrics.distanceMeters else 0.0,
            durationSeconds = if (tripStatus != TripStatus.IDLE) activeMetrics.durationSeconds else 0L,
            avgSpeedKmh = if (tripStatus != TripStatus.IDLE) activeMetrics.avgSpeedKmh else 0.0,
            maxSpeedKmh = if (tripStatus != TripStatus.IDLE) activeMetrics.maxSpeedKmh else 0.0,
            unit = settings.speedUnit,
            altitudeMeters = liveGps.altitude,
            accuracyMeters = liveGps.accuracy
        )

        // Quick Navigation to Map
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateToLiveMap),
            elevation = 3.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = WafaIcons.Map,
                            contentDescription = "Map",
                            tint = CyanNeon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Live Route Map",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (tripStatus == TripStatus.TRACKING) "Active route drawing enabled" else "Tap to open full live map",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                Icon(
                    imageVector = WafaIcons.Crosshair,
                    contentDescription = "Open Map",
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Discard Trip Confirmation Dialog
    if (showDiscardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirmDialog = false },
            title = { Text("Discard Active Trip?") },
            text = { Text("Are you sure you want to discard this trip? All recorded route points and distance for this session will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirmDialog = false
                        kotlinx.coroutines.runBlocking {
                            stateMachine.discardTrip()
                            TripTrackingService.stopService(context)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Discard Trip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirmDialog = false }) {
                    Text("Keep Recording")
                }
            }
        )
    }
}

@Composable
private fun TripControlBar(
    status: TripStatus,
    onStartTrip: () -> Unit,
    onPauseTrip: () -> Unit,
    onResumeTrip: () -> Unit,
    onStopAndSaveTrip: () -> Unit,
    onDiscardTrip: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when (status) {
            TripStatus.IDLE -> {
                Button(
                    onClick = onStartTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_trip_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintSuccess)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = WafaIcons.Play,
                            contentDescription = "Start",
                            tint = Color(0xFF003828)
                        )
                        Text(
                            text = "START TRIP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color(0xFF003828)
                        )
                    }
                }
            }

            TripStatus.TRACKING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPauseTrip,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("pause_trip_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberSpeed)
                    ) {
                        Icon(
                            imageVector = WafaIcons.Pause,
                            contentDescription = "Pause",
                            tint = Color(0xFF432C00)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PAUSE",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF432C00)
                        )
                    }

                    Button(
                        onClick = onStopAndSaveTrip,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("stop_save_trip_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Icon(
                            imageVector = WafaIcons.Stop,
                            contentDescription = "Stop",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STOP & SAVE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            TripStatus.PAUSED -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onResumeTrip,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("resume_trip_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MintSuccess)
                        ) {
                            Icon(
                                imageVector = WafaIcons.Play,
                                contentDescription = "Resume",
                                tint = Color(0xFF003828)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RESUME",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF003828)
                            )
                        }

                        Button(
                            onClick = onStopAndSaveTrip,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("stop_save_trip_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Icon(
                                imageVector = WafaIcons.Stop,
                                contentDescription = "Stop and Save",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STOP & SAVE",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onDiscardTrip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Discard Trip",
                            color = DangerRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun HudLandscapeSpeedometer(
    speed: Double,
    unit: SpeedUnit,
    speedLimit: Double,
    distanceMeters: Double,
    durationSeconds: Long,
    isOverSpeed: Boolean,
    onCloseHud: () -> Unit
) {
    val displaySpeed = speed * unit.multiplierFromKmh
    val distKm = (distanceMeters / 1000.0) * unit.multiplierFromKmh

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        // Exit HUD Button
        IconButton(
            onClick = onCloseHud,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
        ) {
            Icon(
                imageVector = WafaIcons.Close,
                contentDescription = "Close HUD Mode",
                tint = Color.White
            )
        }

        // Central Huge Speed Readout
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%.0f".format(displaySpeed),
                fontSize = 110.sp,
                fontWeight = FontWeight.Black,
                color = if (isOverSpeed) DangerRed else CyanNeon
            )
            Text(
                text = unit.label.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )
        }

        // Bottom Telemetry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SPEED LIMIT", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(text = "${speedLimit.toInt()} ${unit.label}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DISTANCE", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(text = "${"%.2f".format(distKm)} km", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DURATION", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(text = "${durationSeconds / 60}m ${durationSeconds % 60}s", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
