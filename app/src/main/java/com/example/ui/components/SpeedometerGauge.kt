package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedUnit
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MintSuccess
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeedKmh: Double,
    maxGaugeSpeed: Double = 180.0,
    speedLimitKmh: Double = 60.0,
    unit: SpeedUnit = SpeedUnit.KMH,
    isAnalog: Boolean = false,
    isMoving: Boolean = false,
    isOverSpeed: Boolean = false,
    onUnitClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displaySpeed = currentSpeedKmh * unit.multiplierFromKmh
    val displayLimit = speedLimitKmh * unit.multiplierFromKmh

    val animatedSpeed by animateFloatAsState(
        targetValue = displaySpeed.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "SpeedAnimation"
    )

    val maxDisplaySpeed = maxGaugeSpeed * unit.multiplierFromKmh
    val speedRatio = (animatedSpeed / maxDisplaySpeed.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isAnalog) {
            AnalogSpeedometerView(
                speedRatio = speedRatio,
                displaySpeed = animatedSpeed,
                unitLabel = unit.label,
                isOverSpeed = isOverSpeed,
                onUnitClick = onUnitClick
            )
        } else {
            DigitalSpeedometerView(
                speed = animatedSpeed,
                unit = unit,
                speedRatio = speedRatio,
                isMoving = isMoving,
                isOverSpeed = isOverSpeed,
                displayLimit = displayLimit,
                onUnitClick = onUnitClick
            )
        }
    }
}

@Composable
private fun DigitalSpeedometerView(
    speed: Float,
    unit: SpeedUnit,
    speedRatio: Float,
    isMoving: Boolean,
    isOverSpeed: Boolean,
    displayLimit: Double,
    onUnitClick: () -> Unit
) {
    val arcColor = when {
        isOverSpeed -> DangerRed
        speedRatio > 0.65f -> AmberSpeed
        else -> CyanNeon
    }

    Box(
        modifier = Modifier
            .size(260.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Glowing circular telemetry arc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background track (240 degree sweep from 150 deg)
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active speed arc
            val sweep = 240f * speedRatio
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyanNeon, AmberSpeed, if (isOverSpeed) DangerRed else CyanNeon)
                    ),
                    startAngle = 150f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Central Speed Numbers
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Moving / Stopped status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isMoving) MintSuccess.copy(alpha = 0.2f) else Color(0xFF334155))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isMoving) "MOVING" else "IDLE",
                    color = if (isMoving) MintSuccess else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Large Digital Speed
            val formattedSpeed = "%.1f".format(speed.coerceAtLeast(0f))
            Text(
                text = formattedSpeed,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isOverSpeed) DangerRed else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("digital_speed_text")
            )

            // Unit Pill (clickable to toggle km/h / mph)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                    .clickable(onClick = onUnitClick)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("speed_unit_toggle_button")
            ) {
                Text(
                    text = unit.label.uppercase(),
                    color = CyanNeon,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Speed Limit Badge
            Text(
                text = "LIMIT: ${displayLimit.toInt()} ${unit.label}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isOverSpeed) DangerRed else Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun AnalogSpeedometerView(
    speedRatio: Float,
    displaySpeed: Float,
    unitLabel: String,
    isOverSpeed: Boolean,
    onUnitClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(260.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension / 2) - 16.dp.toPx()

            // Outer dial track
            drawCircle(
                color = Color(0xFF1E293B),
                radius = radius,
                style = Stroke(width = 8.dp.toPx())
            )

            // Tick marks
            val totalTicks = 24
            val startAngle = 150.0
            val sweepTotal = 240.0

            for (i in 0..totalTicks) {
                val fraction = i.toDouble() / totalTicks
                val angleDeg = startAngle + (sweepTotal * fraction)
                val angleRad = Math.toRadians(angleDeg)

                val isMajor = i % 4 == 0
                val tickLength = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                val tickColor = if (isMajor) CyanNeon else Color(0xFF64748B)

                val startX = center.x + ((radius - tickLength) * cos(angleRad)).toFloat()
                val startY = center.y + ((radius - tickLength) * sin(angleRad)).toFloat()
                val endX = center.x + (radius * cos(angleRad)).toFloat()
                val endY = center.y + (radius * sin(angleRad)).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                )
            }

            // Needle angle calculation
            val needleAngleDeg = startAngle + (sweepTotal * speedRatio.toDouble())
            val needleAngleRad = Math.toRadians(needleAngleDeg)
            val needleLength = radius * 0.75f
            val needleEnd = Offset(
                center.x + (needleLength * cos(needleAngleRad)).toFloat(),
                center.y + (needleLength * sin(needleAngleRad)).toFloat()
            )

            val needleColor = if (isOverSpeed) DangerRed else AmberSpeed

            drawLine(
                color = needleColor,
                start = center,
                end = needleEnd,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center pin
            drawCircle(
                color = needleColor,
                radius = 7.dp.toPx(),
                center = center
            )
        }

        // Small bottom readout
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${displaySpeed.toInt()}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isOverSpeed) DangerRed else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unitLabel,
                fontSize = 11.sp,
                color = CyanNeon,
                modifier = Modifier.clickable(onClick = onUnitClick)
            )
        }
    }
}
