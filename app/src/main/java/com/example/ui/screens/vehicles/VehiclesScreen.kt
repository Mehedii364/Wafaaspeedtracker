package com.example.ui.screens.vehicles

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.db.entities.VehicleEntity
import com.example.data.model.VehicleType
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.WafaIcons
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintSuccess

@Composable
fun VehiclesScreen(
    onNavigateToDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val vehicleRepo = app.vehicleRepository
    val vehicles by vehicleRepo.allVehicles.collectAsStateWithLifecycle(emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vehicle Garage",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${vehicles.size} vehicles registered",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            if (vehicles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add your first vehicle.",
                        fontSize = 16.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onSetDefault = {
                                kotlinx.coroutines.runBlocking {
                                    vehicleRepo.setDefaultVehicle(vehicle.id)
                                }
                            },
                            onClick = { onNavigateToDetails(vehicle.id) }
                        )
                    }
                }
            }
        }

        // Add Vehicle FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = CyanNeon,
            contentColor = Color(0xFF0F172A)
        ) {
            Icon(imageVector = WafaIcons.Add, contentDescription = "Add Vehicle")
        }
    }

    if (showAddDialog) {
        AddVehicleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, type, reg, odo, notes ->
                kotlinx.coroutines.runBlocking {
                    vehicleRepo.insertVehicle(
                        VehicleEntity(
                            name = name,
                            type = type,
                            registrationNumber = reg,
                            odometerKm = odo,
                            notes = notes,
                            isDefault = vehicles.isEmpty()
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: VehicleEntity,
    onSetDefault: () -> Unit,
    onClick: () -> Unit
) {
    NeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (vehicle.isDefault) CyanNeon.copy(alpha = 0.2f) else Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = WafaIcons.Vehicle,
                        contentDescription = "Vehicle",
                        tint = if (vehicle.isDefault) CyanNeon else Color(0xFF94A3B8),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = vehicle.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (vehicle.isDefault) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyanNeon.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanNeon
                                )
                            }
                        }
                    }

                    Text(
                        text = "${vehicle.type.displayName} • ${vehicle.registrationNumber}",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Text(
                        text = "Odometer: ${"%.1f".format(vehicle.odometerKm)} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MintSuccess
                    )
                }
            }

            if (!vehicle.isDefault) {
                TextButton(onClick = onSetDefault) {
                    Text("Set Active", fontSize = 12.sp, color = CyanNeon)
                }
            }
        }
    }
}

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, type: VehicleType, reg: String, odo: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VehicleType.CAR) }
    var regNumber by remember { mutableStateOf("") }
    var odoText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Vehicle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Vehicle Name (e.g. Daily Sedan)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it },
                    label = { Text("Registration Number / Plate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = odoText,
                    onValueChange = { odoText = it },
                    label = { Text("Current Odometer (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Vehicle Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(VehicleType.CAR, VehicleType.MOTORCYCLE, VehicleType.TRUCK).forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedType = type }
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(text = type.displayName, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val odo = odoText.toDoubleOrNull() ?: 0.0
                        onAdd(name, selectedType, regNumber, odo, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
            ) {
                Text("Add Vehicle", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
