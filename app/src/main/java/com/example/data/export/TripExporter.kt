package com.example.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.db.entities.LocationPointEntity
import com.example.data.db.entities.TripEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TripExporter {

    fun generateGpx(trip: TripEntity, points: List<LocationPointEntity>): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("""<gpx version="1.1" creator="Wafa Speed Tracker - Mehedi364" xmlns="http://www.topografix.com/GPX/1/1">""").append("\n")
        sb.append("  <metadata>\n")
        sb.append("    <name>Trip #${trip.id}</name>\n")
        sb.append("    <time>${isoFormat.format(Date(trip.startTime))}</time>\n")
        sb.append("  </metadata>\n")
        sb.append("  <trk>\n")
        sb.append("    <name>Wafa Trip ${trip.id}</name>\n")
        sb.append("    <trkseg>\n")
        for (p in points) {
            sb.append("""      <trkpt lat="${p.latitude}" lon="${p.longitude}">""").append("\n")
            sb.append("        <ele>${p.altitude}</ele>\n")
            sb.append("        <time>${isoFormat.format(Date(p.timestamp))}</time>\n")
            sb.append("        <speed>${"%.2f".format(p.speedKmh / 3.6)}</speed>\n")
            sb.append("      </trkpt>\n")
        }
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>")
        return sb.toString()
    }

    fun generateCsv(trip: TripEntity, points: List<LocationPointEntity>): String {
        val sb = StringBuilder()
        sb.append("Timestamp,Latitude,Longitude,Speed_KMH,Bearing,Altitude_M,Accuracy_M,IsStop\n")
        for (p in points) {
            sb.append("${p.timestamp},${p.latitude},${p.longitude},${"%.2f".format(p.speedKmh)},${p.bearing},${p.altitude},${p.accuracy},${p.isStopPoint}\n")
        }
        return sb.toString()
    }

    fun generateJson(trip: TripEntity, points: List<LocationPointEntity>): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("""  "trip_id": ${trip.id},""").append("\n")
        sb.append("""  "start_time": ${trip.startTime},""").append("\n")
        sb.append("""  "end_time": ${trip.endTime},""").append("\n")
        sb.append("""  "distance_km": ${"%.2f".format(trip.distanceMeters / 1000.0)},""").append("\n")
        sb.append("""  "duration_seconds": ${trip.durationSeconds},""").append("\n")
        sb.append("""  "moving_seconds": ${trip.movingTimeSeconds},""").append("\n")
        sb.append("""  "stopped_seconds": ${trip.stoppedTimeSeconds},""").append("\n")
        sb.append("""  "max_speed_kmh": ${"%.1f".format(trip.maxSpeedKmh)},""").append("\n")
        sb.append("""  "avg_speed_kmh": ${"%.1f".format(trip.avgSpeedKmh)},""").append("\n")
        sb.append("""  "speed_limit_kmh": ${trip.speedLimitKmh},""").append("\n")
        sb.append("""  "high_speed_events": ${trip.highSpeedEventsCount},""").append("\n")
        sb.append("""  "points_count": ${points.size}""").append("\n")
        sb.append("}")
        return sb.toString()
    }

    fun shareExportFile(
        context: Context,
        content: String,
        fileName: String,
        mimeType: String = "text/plain"
    ) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)
            file.writeText(content)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Export Trip Data")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Fallback plain share text if FileProvider is not configured
            val plainIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content.take(3000))
                putExtra(Intent.EXTRA_SUBJECT, fileName)
            }
            val chooser = Intent.createChooser(plainIntent, "Export Trip Summary")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }
}
