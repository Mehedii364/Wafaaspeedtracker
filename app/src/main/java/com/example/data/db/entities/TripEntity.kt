package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.TripStatus

@Entity(
    tableName = "trips",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["vehicleId"]),
        Index(value = ["startTime"]),
        Index(value = ["status"])
    ]
)
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val movingTimeSeconds: Long = 0L,
    val stoppedTimeSeconds: Long = 0L,
    val maxSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val status: TripStatus = TripStatus.IDLE,
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val endLatitude: Double = 0.0,
    val endLongitude: Double = 0.0,
    val startAddress: String = "",
    val endAddress: String = "",
    val speedLimitKmh: Double = 60.0,
    val highSpeedEventsCount: Int = 0,
    val notes: String = "",
    val aiSummary: String? = null
)
