package com.example.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.ui.components.NeumorphicCard
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val tripRepo = app.tripRepository
    val settingsRepo = app.settingsRepository

    val tripsCount by tripRepo.totalTripsCount.collectAsStateWithLifecycle(0)
    val totalDistMeters by tripRepo.totalDistanceMeters.collectAsStateWithLifecycle(0.0)
    val totalDurationSec by tripRepo.totalDurationSeconds.collectAsStateWithLifecycle(0L)
    val maxSpeedOverall by tripRepo.maxSpeedOverall.collectAsStateWithLifecycle(0.0)
    val avgSpeedOverall by tripRepo.avgSpeedOverall.collectAsStateWithLifecycle(0.0)
    val allTrips by tripRepo.completedTrips.collectAsStateWithLifecycle(emptyList())
    val settings by settingsRepo.settings.collectAsStateWithLifecycle()

    val distKm = (totalDistMeters ?: 0.0) / 1000.0 * settings.speedUnit.multiplierFromKmh
    val totalHours = (totalDurationSec ?: 0L) / 3600.0
    val avgTripDist = if (tripsCount > 0) distKm / tripsCount else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Driving Analytics",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 4 Large Key Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "TOTAL DISTANCE",
                value = "%.1f".format(distKm),
                unit = if (settings.speedUnit.label == "km/h") "km" else "mi",
                color = CyanNeon,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "TOTAL TIME",
                value = "%.1f".format(totalHours),
                unit = "hrs",
                color = MintSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "TOTAL TRIPS",
                value = "$tripsCount",
                unit = "trips",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "MAX RECORDED",
                value = "%.1f".format((maxSpeedOverall ?: 0.0) * settings.speedUnit.multiplierFromKmh),
                unit = settings.speedUnit.label,
                color = AmberSpeed,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "AVG SPEED",
                value = "%.1f".format((avgSpeedOverall ?: 0.0) * settings.speedUnit.multiplierFromKmh),
                unit = settings.speedUnit.label,
                color = CyanNeon,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "AVG TRIP DIST",
                value = "%.1f".format(avgTripDist),
                unit = if (settings.speedUnit.label == "km/h") "km" else "mi",
                color = MintSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        // Distance Trend Bar Chart
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "RECENT TRIPS DISTANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                val recentTrips = allTrips.take(8).reversed()
                if (recentTrips.isEmpty()) {
                    Text("No trip history data yet to generate charts.", fontSize = 12.sp, color = Color(0xFF64748B))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val maxDist = (recentTrips.maxOfOrNull { it.distanceMeters } ?: 1000.0).toFloat().coerceAtLeast(100f)
                            val barWidth = (size.width / (recentTrips.size * 2f)).coerceIn(12f, 32f)
                            val step = size.width / recentTrips.size

                            recentTrips.forEachIndexed { i, trip ->
                                val barHeight = (trip.distanceMeters.toFloat() / maxDist) * size.height * 0.85f
                                val x = (i * step) + (step / 2) - (barWidth / 2)
                                val y = size.height - barHeight

                                drawRoundRect(
                                    color = CyanNeon,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6f, 6f)
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
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(modifier = modifier, elevation = 2.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF94A3B8)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = color
                )
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
