package com.example.ui.screens.maintenance

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WafaApplication
import com.example.data.db.entities.MaintenanceEntity
import com.example.data.model.MaintenanceCategory
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
fun MaintenanceScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val maintenanceRepo = app.maintenanceRepository
    val vehicleRepo = app.vehicleRepository

    val records by maintenanceRepo.allRecords.collectAsStateWithLifecycle(emptyList())
    val totalCost by maintenanceRepo.totalCost.collectAsStateWithLifecycle(0.0)
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
                        text = "Maintenance Manager",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Service schedules & overhaul log",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "LIFETIME SERVICE COST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        Text(
                            text = "$${"%.2f".format(totalCost ?: 0.0)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CyanNeon
                        )
                    }
                    Text(
                        text = "${records.size} log entries",
                        fontSize = 12.sp,
                        color = MintSuccess
                    )
                }
            }

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No maintenance history yet.",
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
                                        text = "${record.category.title} • ${dateFormat.format(Date(record.timestamp))}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Service Odometer: ${"%.0f".format(record.odometerKm)} km",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    record.nextDueOdometerKm?.let { nextOdo ->
                                        Text(
                                            text = "Next Due: ${"%.0f".format(nextOdo)} km",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AmberSpeed
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$${"%.2f".format(record.cost)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyanNeon
                                    )
                                    IconButton(
                                        onClick = {
                                            kotlinx.coroutines.runBlocking {
                                                maintenanceRepo.deleteRecord(record)
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

        // Add Service Record FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = CyanNeon,
            contentColor = Color(0xFF0F172A)
        ) {
            Icon(imageVector = WafaIcons.Add, contentDescription = "Log Service")
        }
    }

    if (showAddDialog) {
        AddMaintenanceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { cat, odo, cost, notes, nextOdo ->
                val vehicleId = vehicles.firstOrNull()?.id ?: 1L
                kotlinx.coroutines.runBlocking {
                    maintenanceRepo.insertRecord(
                        MaintenanceEntity(
                            vehicleId = vehicleId,
                            category = cat,
                            odometerKm = odo,
                            cost = cost,
                            notes = notes,
                            nextDueOdometerKm = nextOdo
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddMaintenanceDialog(
    onDismiss: () -> Unit,
    onAdd: (cat: MaintenanceCategory, odo: Double, cost: Double, notes: String, nextOdo: Double?) -> Unit
) {
    var selectedCat by remember { mutableStateOf(MaintenanceCategory.ENGINE_OIL) }
    var odoText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var nextOdoText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Maintenance Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Category Picker
                Text("Select Category:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(MaintenanceCategory.ENGINE_OIL, MaintenanceCategory.SERVICE, MaintenanceCategory.BRAKES).forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedCat = cat }
                        ) {
                            RadioButton(selected = selectedCat == cat, onClick = { selectedCat = cat })
                            Text(text = cat.title.take(8), fontSize = 10.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = odoText,
                    onValueChange = { odoText = it },
                    label = { Text("Current Odometer (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Cost ($)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextOdoText,
                    onValueChange = { nextOdoText = it },
                    label = { Text("Next Due Odometer (km)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val odo = odoText.toDoubleOrNull() ?: 0.0
                    val cost = costText.toDoubleOrNull() ?: 0.0
                    val nextOdo = nextOdoText.toDoubleOrNull()
                    onAdd(selectedCat, odo, cost, notes, nextOdo)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon)
            ) {
                Text("Save Entry", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
