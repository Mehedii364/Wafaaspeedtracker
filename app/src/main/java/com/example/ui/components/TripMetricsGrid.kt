package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedUnit
import com.example.ui.theme.CyanNeon

@Composable
fun TripMetricsGrid(
    distanceMeters: Double,
    durationSeconds: Long,
    avgSpeedKmh: Double,
    maxSpeedKmh: Double,
    unit: SpeedUnit = SpeedUnit.KMH,
    altitudeMeters: Double = 0.0,
    accuracyMeters: Float = 0f,
    modifier: Modifier = Modifier
) {
    val distVal = (distanceMeters / 1000.0) * unit.multiplierFromKmh
    val distLabel = if (unit == SpeedUnit.KMH) "km" else "mi"

    val avgVal = avgSpeedKmh * unit.multiplierFromKmh
    val maxVal = maxSpeedKmh * unit.multiplierFromKmh

    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    val durationText = if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(
                title = "DISTANCE",
                value = "%.2f".format(distVal),
                unit = distLabel,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "DURATION",
                value = durationText,
                unit = "time",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(
                title = "AVG SPEED",
                value = "%.1f".format(avgVal),
                unit = unit.label,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "MAX SPEED",
                value = "%.1f".format(maxVal),
                unit = unit.label,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricTile(
                title = "ALTITUDE",
                value = "%.0f".format(altitudeMeters),
                unit = "m",
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "GPS ACCURACY",
                value = "±%.1f".format(accuracyMeters),
                unit = "m",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricTile(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(
        modifier = modifier,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF94A3B8)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanNeon
                )
            }
        }
    }
}
