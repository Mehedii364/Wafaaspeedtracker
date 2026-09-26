package com.example.data.ai

import com.example.data.db.entities.TripEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiTripSummaryRequest(
    val distanceKm: Double,
    val durationMinutes: Long,
    val movingMinutes: Long,
    val stoppedMinutes: Long,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val speedLimitKmh: Double,
    val highSpeedEvents: Int
)

data class AiAnalysisResult(
    val isSuccess: Boolean,
    val source: String, // "Remote AI Backend" or "On-Device Smart Analyzer"
    val summary: String,
    val drivingStyle: String,
    val efficiencyScore: Int, // 0 - 100
    val safetyTip: String,
    val disclaimer: String = "AI insights are analytical interpretations and not certified vehicle-safety measurements."
)

class WafaAiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    suspend fun analyzeTrip(
        trip: TripEntity,
        backendUrl: String?
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val distKm = trip.distanceMeters / 1000.0
        val durationMins = (trip.durationSeconds / 60).coerceAtLeast(1)
        val movingMins = trip.movingTimeSeconds / 60
        val stoppedMins = trip.stoppedTimeSeconds / 60

        // If user configured a secure private backend URL, attempt network call
        if (!backendUrl.isNullOrBlank() && backendUrl.startsWith("http")) {
            val remoteResult = tryRemoteBackend(trip, backendUrl, distKm, durationMins, movingMins, stoppedMins)
            if (remoteResult != null) return@withContext remoteResult
        }

        // On-Device Intelligent Rule & Pattern Analyzer (Offline-first fallback)
        return@withContext analyzeOnDevice(trip, distKm, durationMins, movingMins, stoppedMins)
    }

    private fun tryRemoteBackend(
        trip: TripEntity,
        backendUrl: String,
        distKm: Double,
        durationMins: Long,
        movingMins: Long,
        stoppedMins: Long
    ): AiAnalysisResult? {
        return try {
            val jsonPayload = JSONObject().apply {
                put("distance_km", distKm)
                put("duration_minutes", durationMins)
                put("moving_minutes", movingMins)
                put("stopped_minutes", stoppedMins)
                put("average_speed_kmh", trip.avgSpeedKmh)
                put("max_speed_kmh", trip.maxSpeedKmh)
                put("speed_limit_kmh", trip.speedLimitKmh)
                put("high_speed_events", trip.highSpeedEventsCount)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(backendUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                val respJson = JSONObject(respBody)
                AiAnalysisResult(
                    isSuccess = true,
                    source = "Wafa AI Cloud Backend",
                    summary = respJson.optString("summary", "Trip processed successfully."),
                    drivingStyle = respJson.optString("driving_style", "Normal Cruising"),
                    efficiencyScore = respJson.optInt("efficiency_score", 85),
                    safetyTip = respJson.optString("safety_tip", "Maintain safe braking distances.")
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun analyzeOnDevice(
        trip: TripEntity,
        distKm: Double,
        durationMins: Long,
        movingMins: Long,
        stoppedMins: Long
    ): AiAnalysisResult {
        var score = 100

        // High-speed event penalties
        if (trip.highSpeedEventsCount > 0) {
            score -= (trip.highSpeedEventsCount * 12).coerceAtMost(40)
        }

        // Excessive speed penalty
        if (trip.maxSpeedKmh > trip.speedLimitKmh + 20) {
            score -= 15
        }

        // Excessive stopped time penalty (idling)
        if (stoppedMins > movingMins && distKm > 2.0) {
            score -= 10
        }

        score = score.coerceIn(20, 100)

        val drivingStyle = when {
            trip.highSpeedEventsCount == 0 && trip.maxSpeedKmh <= trip.speedLimitKmh -> "Smooth & Defensive"
            trip.highSpeedEventsCount in 1..2 -> "Moderate Highway Cruiser"
            else -> "Aggressive / Dynamic"
        }

        val summary = buildString {
            append("Completed a ${"%.1f".format(distKm)} km journey in $durationMins minutes ")
            append("with an average speed of ${"%.1f".format(trip.avgSpeedKmh)} km/h. ")
            if (trip.highSpeedEventsCount > 0) {
                append("Recorded ${trip.highSpeedEventsCount} speed limit exceedance event(s). ")
            } else {
                append("Exemplary adherence to configured speed limits throughout the trip. ")
            }
            if (stoppedMins > 0) {
                append("Stationary time: $stoppedMins minutes.")
            }
        }

        val safetyTip = when {
            trip.highSpeedEventsCount > 2 -> "Frequent speed bursts increase fuel consumption by up to 25%. Try smoother acceleration."
            trip.maxSpeedKmh > trip.speedLimitKmh -> "Watch out for sudden road hazards at speeds above ${trip.speedLimitKmh.toInt()} km/h."
            stoppedMins > 15 -> "Consider switching off the engine during long stationary pauses to conserve fuel."
            else -> "Excellent speed control and smooth pacing maintained throughout this journey."
        }

        return AiAnalysisResult(
            isSuccess = true,
            source = "Wafa AI Smart Engine (Local)",
            summary = summary,
            drivingStyle = drivingStyle,
            efficiencyScore = score,
            safetyTip = safetyTip
        )
    }

    fun answerQuery(
        query: String,
        trips: List<TripEntity>
    ): String {
        val q = query.lowercase()
        return when {
            q.contains("longest") || q.contains("highest distance") -> {
                val longest = trips.maxByOrNull { it.distanceMeters }
                if (longest != null) {
                    "Your longest recorded trip is #${longest.id} covering ${"%.1f".format(longest.distanceMeters / 1000.0)} km with an average speed of ${"%.1f".format(longest.avgSpeedKmh)} km/h."
                } else {
                    "No completed trips found yet to calculate the longest journey."
                }
            }
            q.contains("fastest") || q.contains("max speed") || q.contains("highest speed") -> {
                val fastest = trips.maxByOrNull { it.maxSpeedKmh }
                if (fastest != null) {
                    "The highest speed recorded across your trips was ${"%.1f".format(fastest.maxSpeedKmh)} km/h during trip #${fastest.id}."
                } else {
                    "No recorded speed records found."
                }
            }
            q.contains("how far") || q.contains("total distance") -> {
                val totalKm = trips.sumOf { it.distanceMeters } / 1000.0
                "You have traveled a total of ${"%.1f".format(totalKm)} km across ${trips.size} trips."
            }
            else -> {
                val totalKm = trips.sumOf { it.distanceMeters } / 1000.0
                val totalHours = trips.sumOf { it.durationSeconds } / 3600.0
                "Summary: ${trips.size} trips recorded, ${"%.1f".format(totalKm)} km total distance, and ${"%.1f".format(totalHours)} hours on the road."
            }
        }
    }
}
