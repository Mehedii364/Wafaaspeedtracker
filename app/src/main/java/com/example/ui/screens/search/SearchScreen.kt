package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.WafaIcons
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SearchFilter(val label: String) {
    ALL("All"),
    TRIPS("Trips"),
    VEHICLES("Vehicles"),
    SPEED_EVENTS("Speed Alerts"),
    FUEL("Fuel"),
    MAINTENANCE("Maintenance")
}

@Composable
fun SearchScreen(
    onNavigateToTrip: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val tripRepo = app.tripRepository
    val vehicleRepo = app.vehicleRepository
    val fuelRepo = app.fuelRepository
    val maintenanceRepo = app.maintenanceRepository

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }

    val trips by tripRepo.completedTrips.collectAsStateWithLifecycle(emptyList())
    val vehicles by vehicleRepo.allVehicles.collectAsStateWithLifecycle(emptyList())
    val fuels by fuelRepo.allRecords.collectAsStateWithLifecycle(emptyList())
    val services by maintenanceRepo.allRecords.collectAsStateWithLifecycle(emptyList())

    val filteredTrips = trips.filter {
        query.isBlank() || it.notes.contains(query, ignoreCase = true) || "Trip #${it.id}".contains(query, ignoreCase = true)
    }

    val filteredVehicles = vehicles.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) || it.registrationNumber.contains(query, ignoreCase = true)
    }

    val filteredFuel = fuels.filter {
        query.isBlank() || it.notes.contains(query, ignoreCase = true) || "${it.liters}".contains(query)
    }

    val filteredServices = services.filter {
        query.isBlank() || it.category.title.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Global Search",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search trips, vehicles, fuel, notes...") },
            leadingIcon = {
                Icon(imageVector = WafaIcons.Search, contentDescription = "Search", tint = CyanNeon)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(imageVector = WafaIcons.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SearchFilter.values()) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) CyanNeon else Color(0xFF131B2F))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Search Results List
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Trips results
            if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.TRIPS) {
                items(filteredTrips, key = { "trip_${it.id}" }) { trip ->
                    NeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTrip(trip.id) }
                    ) {
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
                                    text = "${dateFormat.format(Date(trip.startTime))} • ${"%.1f".format(trip.distanceMeters / 1000.0)} km",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = "TRIP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                    }
                }
            }

            // Vehicles results
            if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.VEHICLES) {
                items(filteredVehicles, key = { "veh_${it.id}" }) { veh ->
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = veh.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${veh.type.displayName} • ${veh.registrationNumber}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = "VEHICLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintSuccess
                            )
                        }
                    }
                }
            }

            // Fuel results
            if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.FUEL) {
                items(filteredFuel, key = { "fuel_${it.id}" }) { fuel ->
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${"%.1f".format(fuel.liters)} L Refill - $${"%.2f".format(fuel.totalCost)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = dateFormat.format(Date(fuel.timestamp)),
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = "FUEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberSpeed
                            )
                        }
                    }
                }
            }

            // Maintenance results
            if (selectedFilter == SearchFilter.ALL || selectedFilter == SearchFilter.MAINTENANCE) {
                items(filteredServices, key = { "maint_${it.id}" }) { service ->
                    NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = service.category.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${dateFormat.format(Date(service.timestamp))} • Cost: $${"%.2f".format(service.cost)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = "SERVICE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                    }
                }
            }
        }
    }
}
