package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MaintenanceCategory

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vehicleId"]),
        Index(value = ["timestamp"])
    ]
)
data class MaintenanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val category: MaintenanceCategory,
    val timestamp: Long = System.currentTimeMillis(),
    val odometerKm: Double,
    val cost: Double = 0.0,
    val notes: String = "",
    val nextDueDate: Long? = null,
    val nextDueOdometerKm: Double? = null,
    val isCompleted: Boolean = true
)
