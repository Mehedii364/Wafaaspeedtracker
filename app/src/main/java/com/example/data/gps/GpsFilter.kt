package com.example.data.gps

import android.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GpsFilter {
    const val MAX_ACCURACY_THRESHOLD_METERS = 35.0f
    const val MAX_REASONABLE_SPEED_KMH = 300.0
    const val STOPPED_SPEED_THRESHOLD_KMH = 1.0 // Below 1 km/h is treated as stationary drift
    const val STALE_LOCATION_THRESHOLD_MS = 15_000L // 15 seconds without update = signal lost

    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }

    /**
     * Alternative pure math Haversine formula (helpful for local JVM unit tests without android.location mock).
     */
    fun haversineDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun isAccuracyAcceptable(accuracy: Float): Boolean {
        return accuracy in 0.1f..MAX_ACCURACY_THRESHOLD_METERS
    }

    fun isSpeedReasonable(speedKmh: Double): Boolean {
        return speedKmh in 0.0..MAX_REASONABLE_SPEED_KMH
    }

    /**
     * Validates whether a consecutive point jump is physically feasible.
     */
    fun isJumpAnomaly(
        prevLat: Double,
        prevLon: Double,
        prevTime: Long,
        currLat: Double,
        currLon: Double,
        currTime: Long
    ): Boolean {
        val deltaSeconds = (currTime - prevTime) / 1000.0
        if (deltaSeconds <= 0.0) return true
        val distance = haversineDistanceMeters(prevLat, prevLon, currLat, currLon)
        val impliedSpeedKmh = (distance / deltaSeconds) * 3.6
        return impliedSpeedKmh > MAX_REASONABLE_SPEED_KMH
    }
}
