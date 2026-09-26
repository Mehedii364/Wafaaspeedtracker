package com.example.ui.screens.history

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.ai.AiAnalysisResult
import com.example.data.db.entities.LocationPointEntity
import com.example.data.db.entities.TripEntity
import com.example.data.export.TripExporter
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.TripMetricsGrid
import com.example.ui.components.WafaIcons
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MintSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripDetailsScreen(
    tripId: Long,
    onBack: () -> Unit,
    onNavigateToReplay: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val tripRepo = app.tripRepository
    val settingsRepo = app.settingsRepository
    val aiService = app.aiService

    var trip by remember { mutableStateOf<TripEntity?>(null) }
    var points by remember { mutableStateOf<List<LocationPointEntity>>(emptyList()) }
    var isAiAnalyzing by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf<AiAnalysisResult?>(null) }

    val settings by settingsRepo.settings.collectAsStateWithLifecycle()

    LaunchedEffect(tripId) {
        trip = tripRepo.getTripById(tripId)
        points = tripRepo.getPointsForTripSync(tripId)
    }

    val currentTrip = trip
    if (currentTrip == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CyanNeon)
        }
        return
    }

    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateText = dateFormat.format(Date(currentTrip.startTime))

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131B2F))
            ) {
                Icon(
                    imageVector = WafaIcons.Back,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Trip #${currentTrip.id}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Replay Button
            IconButton(
                onClick = { onNavigateToReplay(currentTrip.id) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyanNeon)
            ) {
                Icon(
                    imageVector = WafaIcons.Replay,
                    contentDescription = "Replay Route",
                    tint = Color(0xFF0F172A)
                )
            }
        }

        Text(
            text = dateText,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        // Route Map Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            InteractiveMapCanvas(
                currentLat = currentTrip.endLatitude.takeIf { it != 0.0 } ?: currentTrip.startLatitude,
                currentLon = currentTrip.endLongitude.takeIf { it != 0.0 } ?: currentTrip.startLongitude,
                bearing = 0f,
                accuracyMeters = 10f,
                routePoints = points,
                isTrackingActive = false
            )
        }

        // Metrics Grid
        TripMetricsGrid(
            distanceMeters = currentTrip.distanceMeters,
            durationSeconds = currentTrip.durationSeconds,
            avgSpeedKmh = currentTrip.avgSpeedKmh,
            maxSpeedKmh = currentTrip.maxSpeedKmh,
            unit = settings.speedUnit
        )

        // Speed Profile Chart
        SpeedProfileChart(points = points, unitLabel = settings.speedUnit.label)

        // Moving vs Stopped Time breakdown
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TIME BREAKDOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Moving Time", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${currentTrip.movingTimeSeconds / 60}m ${currentTrip.movingTimeSeconds % 60}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MintSuccess
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Stopped Time", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${currentTrip.stoppedTimeSeconds / 60}m ${currentTrip.stoppedTimeSeconds % 60}s",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AmberSpeed
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Recorded Points", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${points.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyanNeon
                        )
                    }
                }
            }
        }

        // Wafa AI Trip Insights Card
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = WafaIcons.Compass,
                            contentDescription = "AI",
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Wafa AI Monitor",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isAiAnalyzing && aiResult == null) {
                        Button(
                            onClick = {
                                isAiAnalyzing = true
                                kotlinx.coroutines.runBlocking {
                                    val res = aiService.analyzeTrip(currentTrip, settings.aiBackendUrl)
                                    aiResult = res
                                    isAiAnalyzing = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Analyze", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isAiAnalyzing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CyanNeon, strokeWidth = 2.dp)
                        Text("Analyzing speed patterns & driving style...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }

                aiResult?.let { res ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Style: ${res.drivingStyle}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MintSuccess)
                            Text(text = "Efficiency: ${res.efficiencyScore}/100", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                        }
                        Text(text = res.summary, fontSize = 12.sp, color = Color(0xFFE2E8F0))
                        Text(text = "💡 Tip: ${res.safetyTip}", fontSize = 11.sp, color = AmberSpeed)
                        Text(text = res.disclaimer, fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        // Export Actions: GPX, CSV, JSON
        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "EXPORT TRIP DATA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val gpx = TripExporter.generateGpx(currentTrip, points)
                            TripExporter.shareExportFile(context, gpx, "trip_${currentTrip.id}.gpx", "application/gpx+xml")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("GPX")
                    }

                    OutlinedButton(
                        onClick = {
                            val csv = TripExporter.generateCsv(currentTrip, points)
                            TripExporter.shareExportFile(context, csv, "trip_${currentTrip.id}.csv", "text/csv")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CSV")
                    }

                    OutlinedButton(
                        onClick = {
                            val json = TripExporter.generateJson(currentTrip, points)
                            TripExporter.shareExportFile(context, json, "trip_${currentTrip.id}.json", "application/json")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("JSON")
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedProfileChart(
    points: List<LocationPointEntity>,
    unitLabel: String
) {
    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SPEED TIMELINE PROFILE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF94A3B8)
            )

            if (points.size < 2) {
                Text("Not enough GPS points to render speed profile.", fontSize = 12.sp, color = Color(0xFF64748B))
                return@Column
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxSpeed = (points.maxOfOrNull { it.speedKmh } ?: 100.0).coerceAtLeast(20.0).toFloat()
                    val chartPath = Path()
                    val fillPath = Path()

                    val stepX = size.width / (points.size - 1)

                    points.forEachIndexed { index, p ->
                        val x = index * stepX
                        val y = size.height - ((p.speedKmh.toFloat() / maxSpeed) * size.height)

                        if (index == 0) {
                            chartPath.moveTo(x, y)
                            fillPath.moveTo(x, size.height)
                            fillPath.lineTo(x, y)
                        } else {
                            chartPath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }

                    fillPath.lineTo(size.width, size.height)
                    fillPath.close()

                    // Gradient under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CyanNeon.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Line stroke
                    drawPath(
                        path = chartPath,
                        color = CyanNeon,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
