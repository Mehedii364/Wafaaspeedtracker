package com.example.ui.screens.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.db.entities.TripEntity
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.WafaIcons
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MintSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripHistoryScreen(
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToReplay: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val tripRepo = app.tripRepository
    val settingsRepo = app.settingsRepository
    val trips by tripRepo.completedTrips.collectAsStateWithLifecycle(emptyList())
    val settings by settingsRepo.settings.collectAsStateWithLifecycle()

    var tripToDelete by remember { mutableStateOf<TripEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Trip History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${trips.size} journeys recorded",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        if (trips.isEmpty()) {
            // Polished Empty State (as required by section 39)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CyanNeon.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = WafaIcons.Speedometer,
                            contentDescription = "No trips",
                            tint = CyanNeon,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "No trips yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Press START TRIP when you are ready to record a journey.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trips, key = { it.id }) { trip ->
                    TripItemCard(
                        trip = trip,
                        speedMultiplier = settings.speedUnit.multiplierFromKmh,
                        unitLabel = settings.speedUnit.label,
                        onClick = { onNavigateToDetails(trip.id) },
                        onReplay = { onNavigateToReplay(trip.id) },
                        onDelete = { tripToDelete = trip }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            title = { Text("Delete Trip #${trip.id}?") },
            text = { Text("This will permanently remove this trip record, route points, and speed statistics from local storage.") },
            confirmButton = {
                Button(
                    onClick = {
                        kotlinx.coroutines.runBlocking {
                            tripRepo.deleteTrip(trip)
                        }
                        tripToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TripItemCard(
    trip: TripEntity,
    speedMultiplier: Double,
    unitLabel: String,
    onClick: () -> Unit,
    onReplay: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(trip.startTime))
    val distVal = (trip.distanceMeters / 1000.0) * speedMultiplier
    val distUnit = if (unitLabel == "km/h") "km" else "mi"
    val minutes = trip.durationSeconds / 60
    val seconds = trip.durationSeconds % 60

    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("trip_card_${trip.id}"),
        elevation = 3.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Date & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trip #${trip.id}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Replay button
                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = WafaIcons.Replay,
                            contentDescription = "Replay route",
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = WafaIcons.Close,
                            contentDescription = "Delete trip",
                            tint = DangerRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DISTANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = "${"%.2f".format(distVal)} $distUnit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DURATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = "${minutes}m ${seconds}s",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "AVG SPEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = "${"%.1f".format(trip.avgSpeedKmh * speedMultiplier)} $unitLabel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanNeon
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "MAX SPEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = "${"%.1f".format(trip.maxSpeedKmh * speedMultiplier)} $unitLabel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = AmberSpeed
                    )
                }
            }

            if (trip.highSpeedEventsCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = WafaIcons.Warning,
                        contentDescription = "Alerts",
                        tint = DangerRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${trip.highSpeedEventsCount} speed limit exceedance event(s)",
                        fontSize = 11.sp,
                        color = DangerRed
                    )
                }
            }
        }
    }
}
