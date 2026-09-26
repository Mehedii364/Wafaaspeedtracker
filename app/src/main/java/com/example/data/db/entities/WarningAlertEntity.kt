package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.WarningLevel
import com.example.data.model.WarningType

@Entity(
    tableName = "warning_alerts",
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["timestamp"])
    ]
)
data class WarningAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val type: WarningType,
    val level: WarningLevel,
    val message: String
)
