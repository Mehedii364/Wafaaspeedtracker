package com.example.data.gps

import com.example.data.db.entities.LocationPointEntity
import com.example.data.model.TripStatus

data class LiveGpsData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speedKmh: Double = 0.0,
    val bearing: Float = 0f,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val timestamp: Long = 0L,
    val isAvailable: Boolean = false,
    val isSignalLost: Boolean = true,
    val isAccuracyPoor: Boolean = false,
    val isMoving: Boolean = false
)

data class ActiveTripMetrics(
    val tripId: Long = 0L,
    val vehicleId: Long? = null,
    val status: TripStatus = TripStatus.IDLE,
    val startTime: Long = 0L,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0L,
    val movingTimeSeconds: Long = 0L,
    val stoppedTimeSeconds: Long = 0L,
    val currentSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val speedLimitKmh: Double = 60.0,
    val isOverSpeed: Boolean = false,
    val highSpeedEventsCount: Int = 0,
    val recordedPointsCount: Int = 0,
    val recentPoints: List<LocationPointEntity> = emptyList()
)
