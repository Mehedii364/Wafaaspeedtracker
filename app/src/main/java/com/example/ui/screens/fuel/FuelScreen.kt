package com.example.ui.screens.fuel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.db.entities.FuelRecordEntity
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
fun FuelScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val fuelRepo = app.fuelRepository
    val vehicleRepo = app.vehicleRepository

    val records by fuelRepo.allRecords.collectAsStateWithLifecycle(emptyList())
    val totalCost by fuelRepo.totalCost.collectAsStateWithLifecycle(0.0)
    val totalLiters by fuelRepo.totalLiters.collectAsStateWithLifecycle(0.0)
    val vehicles by vehicleRepo.allVehicles.collectAsStateWithLifecycle(emptyList())

    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Fuel Management",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeumorphicCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(text = "TOTAL SPENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text(
                            text = "$${"%.2f".format(totalCost ?: 0.0)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = AmberSpeed
                        )
                    }
                }
                NeumorphicCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(text = "TOTAL VOLUME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text(
                            text = "${"%.1f".format(totalLiters ?: 0.0)} L",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CyanNeon
                        )
                    }
                }
            }

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No fuel entries yet.",
                        fontSize = 16.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(records, key = { it.id }) { record ->
                        NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = dateFormat.format(Date(record.timestamp)),
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = "${"%.1f".format(record.liters)} Litres @ $${"%.2f".format(record.pricePerLiter)}/L",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Odometer: ${"%.0f".format(record.odometerKm)} km",
                                        fontSize = 11.sp,
                                        color = MintSuccess
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$${"%.2f".format(record.totalCost)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = AmberSpeed
                                    )
                                    IconButton(
                                        onClick = {
                                            kotlinx.coroutines.runBlocking {
                                                fuelRepo.deleteRecord(record)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = WafaIcons.Close,
                                            contentDescription = "Delete",
                                            tint = DangerRed.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Fuel Record FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = AmberSpeed,
            contentColor = Color(0xFF432C00)
        ) {
            Icon(imageVector = WafaIcons.Add, contentDescription = "Add Fuel Record")
        }
    }

    if (showAddDialog) {
        AddFuelDialog(
            vehicles = vehicles,
            onDismiss = { showAddDialog = false },
            onAdd = { vehicleId, liters, price, odo, notes ->
                kotlinx.coroutines.runBlocking {
                    fuelRepo.insertRecord(
                        FuelRecordEntity(
                            vehicleId = vehicleId,
                            liters = liters,
                            pricePerLiter = price,
                            totalCost = liters * price,
                            odometerKm = odo,
                            notes = notes
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddFuelDialog(
    vehicles: List<com.example.data.db.entities.VehicleEntity>,
    onDismiss: () -> Unit,
    onAdd: (vehicleId: Long, liters: Double, price: Double, odo: Double, notes: String) -> Unit
) {
    var litersText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var odoText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val defaultVehicleId = vehicles.firstOrNull { it.isDefault }?.id
        ?: vehicles.firstOrNull()?.id ?: 1L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Fuel Refill") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = litersText,
                    onValueChange = { litersText = it },
                    label = { Text("Litres Refilled") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price Per Litre ($)") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val liters = litersText.toDoubleOrNull() ?: 0.0
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val odo = odoText.toDoubleOrNull() ?: 0.0
                    if (liters > 0) {
                        onAdd(defaultVehicleId, liters, price, odo, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberSpeed)
            ) {
                Text("Save Record", color = Color(0xFF432C00), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
