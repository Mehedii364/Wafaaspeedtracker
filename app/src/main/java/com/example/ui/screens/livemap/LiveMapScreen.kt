package com.example.ui.screens.livemap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.TripStatus
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.CyanNeon

@Composable
fun LiveMapScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val stateMachine = app.tripStateMachine
    val settingsRepo = app.settingsRepository

    val liveGps by stateMachine.liveGps.collectAsStateWithLifecycle()
    val activeMetrics by stateMachine.activeMetrics.collectAsStateWithLifecycle()
    val tripStatus by stateMachine.status.collectAsStateWithLifecycle()
    val settings by settingsRepo.settings.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        // Fullscreen Interactive Vector & Tile Map Canvas
        InteractiveMapCanvas(
            currentLat = liveGps.latitude,
            currentLon = liveGps.longitude,
            bearing = liveGps.bearing,
            accuracyMeters = liveGps.accuracy,
            routePoints = activeMetrics.recentPoints,
            isTrackingActive = tripStatus == TripStatus.TRACKING
        )

        // Floating Bottom HUD Panel: Live Speed & Distance Summary
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(
                        text = "SPEED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    val speed = if (tripStatus == TripStatus.TRACKING) activeMetrics.currentSpeedKmh else liveGps.speedKmh
                    Text(
                        text = "${"%.1f".format(speed * settings.speedUnit.multiplierFromKmh)} ${settings.speedUnit.label}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = CyanNeon
                    )
                }

                if (tripStatus != TripStatus.IDLE) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .background(Color(0xFF334155))
                    )
                    Column {
                        Text(
                            text = "TRIP DISTANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        val distKm = (activeMetrics.distanceMeters / 1000.0) * settings.speedUnit.multiplierFromKmh
                        Text(
                            text = "${"%.2f".format(distKm)} ${if (settings.speedUnit.label == "km/h") "km" else "mi"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
