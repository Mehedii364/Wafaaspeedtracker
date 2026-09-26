package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.VehicleType

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: VehicleType,
    val registrationNumber: String = "",
    val odometerKm: Double = 0.0,
    val notes: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
