package com.example.ui.screens.replay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.db.entities.LocationPointEntity
import com.example.data.db.entities.TripEntity
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.WafaIcons
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripReplayScreen(
    tripId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val tripRepo = app.tripRepository
    val settingsRepo = app.settingsRepository

    var trip by remember { mutableStateOf<TripEntity?>(null) }
    var points by remember { mutableStateOf<List<LocationPointEntity>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackSpeedMultiplier by remember { mutableFloatStateOf(1f) }

    val settings by settingsRepo.settings.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        trip = tripRepo.getTripById(tripId)
        points = tripRepo.getPointsForTripSync(tripId)
    }

    // Playback loop
    LaunchedEffect(isPlaying, currentIndex, playbackSpeedMultiplier, points.size) {
        if (isPlaying && points.isNotEmpty()) {
            val delayMs = (400L / playbackSpeedMultiplier).toLong().coerceAtLeast(50L)
            while (isPlaying && currentIndex < points.size - 1) {
                delay(delayMs)
                currentIndex++
            }
            if (currentIndex >= points.size - 1) {
                isPlaying = false
            }
        }
    }

    val currentPoint = points.getOrNull(currentIndex)
    if (trip == null || points.isEmpty() || currentPoint == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyanNeon)
        }
        return
    }

    val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    val pointTime = timeFormat.format(Date(currentPoint.timestamp))

    Box(modifier = modifier.fillMaxSize()) {
        // Map canvas replaying route up to current index
        val sublistPoints = points.take(currentIndex + 1)
        InteractiveMapCanvas(
            currentLat = currentPoint.latitude,
            currentLon = currentPoint.longitude,
            bearing = currentPoint.bearing,
            accuracyMeters = currentPoint.accuracy,
            routePoints = sublistPoints,
            isTrackingActive = false
        )

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            ) {
                Icon(
                    imageVector = WafaIcons.Back,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "REPLAY: TRIP #${trip?.id}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
            }
        }

        // Bottom Controls Deck
        NeumorphicCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Telemetry line: Speed & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "CURRENT SPEED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text(
                            text = "${"%.1f".format(currentPoint.speedKmh * settings.speedUnit.multiplierFromKmh)} ${settings.speedUnit.label}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CyanNeon
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "TIMESTAMP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Text(
                            text = pointTime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Progress Slider
                Slider(
                    value = currentIndex.toFloat(),
                    onValueChange = {
                        isPlaying = false
                        currentIndex = it.toInt()
                    },
                    valueRange = 0f..(points.size - 1).toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanNeon,
                        activeTrackColor = CyanNeon,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Playback Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Restart
                    IconButton(
                        onClick = {
                            currentIndex = 0
                            isPlaying = true
                        }
                    ) {
                        Icon(
                            imageVector = WafaIcons.Replay,
                            contentDescription = "Restart Replay",
                            tint = Color.White
                        )
                    }

                    // Play / Pause
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(CyanNeon)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) WafaIcons.Pause else WafaIcons.Play,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Speed Multiplier (1x, 2x, 5x)
                    IconButton(
                        onClick = {
                            playbackSpeedMultiplier = when (playbackSpeedMultiplier) {
                                1f -> 2f
                                2f -> 5f
                                else -> 1f
                            }
                        }
                    ) {
                        Text(
                            text = "${playbackSpeedMultiplier.toInt()}x",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberSpeed
                        )
                    }
                }
            }
        }
    }
}
