package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.LocationPointEntity
import com.example.ui.theme.AmberSpeed
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MintSuccess
import kotlin.math.cos
import kotlin.math.sin

enum class MapLayerType(val label: String) {
    STANDARD("Cockpit"),
    SATELLITE("Satellite"),
    TERRAIN("Topographic")
}

@Composable
fun InteractiveMapCanvas(
    currentLat: Double,
    currentLon: Double,
    bearing: Float,
    accuracyMeters: Float,
    routePoints: List<LocationPointEntity>,
    isTrackingActive: Boolean,
    modifier: Modifier = Modifier,
    initialZoom: Float = 14f,
    onMapLayerChange: ((MapLayerType) -> Unit)? = null
) {
    var zoomLevel by remember { mutableFloatStateOf(initialZoom) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var isFollowingVehicle by remember { mutableStateOf(true) }
    var selectedLayer by remember { mutableStateOf(MapLayerType.STANDARD) }

    // If following vehicle, reset pan offset
    if (isFollowingVehicle) {
        panOffsetX = 0f
        panOffsetY = 0f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(
                when (selectedLayer) {
                    MapLayerType.STANDARD -> Color(0xFF0F172A)
                    MapLayerType.SATELLITE -> Color(0xFF07111E)
                    MapLayerType.TERRAIN -> Color(0xFF131D1F)
                }
            )
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    isFollowingVehicle = false
                    panOffsetX += dragAmount.x
                    panOffsetY += dragAmount.y
                }
            }
    ) {
        // Map Grid & Route Rendering Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + panOffsetX, size.height / 2 + panOffsetY)

            // Draw Layer Background Grid
            drawMapGrid(selectedLayer, size)

            // Conversion scale from GPS lat/lon degrees to screen pixels based on zoomLevel
            val scale = 110_000f * (zoomLevel / 14f) * 0.003f

            // Reference point for projection (center of the canvas is user's current GPS)
            val refLat = if (currentLat != 0.0) currentLat else 23.8103
            val refLon = if (currentLon != 0.0) currentLon else 90.4125

            // Draw recorded route polyline
            if (routePoints.isNotEmpty()) {
                val polyPath = Path()
                var first = true

                for (p in routePoints) {
                    val px = center.x + ((p.longitude - refLon) * scale).toFloat()
                    val py = center.y - ((p.latitude - refLat) * scale).toFloat()

                    if (first) {
                        polyPath.moveTo(px, py)
                        first = false
                    } else {
                        polyPath.lineTo(px, py)
                    }
                }

                // Polyline gradient path
                drawPath(
                    path = polyPath,
                    brush = Brush.linearGradient(
                        colors = listOf(CyanNeon, AmberSpeed, MintSuccess)
                    ),
                    style = Stroke(
                        width = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Start Marker (green)
                val start = routePoints.first()
                val startX = center.x + ((start.longitude - refLon) * scale).toFloat()
                val startY = center.y - ((start.latitude - refLat) * scale).toFloat()
                drawCircle(
                    color = MintSuccess,
                    radius = 8.dp.toPx(),
                    center = Offset(startX, startY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(startX, startY)
                )
            }

            // Current Location Marker
            if (currentLat != 0.0 && currentLon != 0.0) {
                // Accuracy Halo
                val accRadiusPx = (accuracyMeters * scale * 0.00001f).coerceIn(12f, 90f)
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.15f),
                    radius = accRadiusPx,
                    center = center
                )
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.4f),
                    radius = accRadiusPx,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Vehicle Direction Marker
                drawVehicleBearingMarker(center, bearing)
            }
        }

        // Top Layer Control Bar: Map Style Selector
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MapLayerType.values().forEach { layer ->
                val isSelected = selectedLayer == layer
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CyanNeon else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = layer.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Compass Indicator (Top Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                .border(1.dp, Color(0xFF334155), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = WafaIcons.Compass,
                contentDescription = "Compass Heading: ${bearing.toInt()}°",
                tint = CyanNeon,
                modifier = Modifier.size(24.dp)
            )
        }

        // Floating Action Controls: Re-Center & Zoom In / Out
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Re-center & Follow Mode Button
            IconButton(
                onClick = {
                    isFollowingVehicle = true
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isFollowingVehicle) CyanNeon else Color(0xFF0F172A).copy(alpha = 0.85f),
                    contentColor = if (isFollowingVehicle) Color(0xFF0F172A) else Color.White
                ),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("map_recenter_button")
            ) {
                Icon(
                    imageVector = WafaIcons.Crosshair,
                    contentDescription = "Follow Vehicle Position"
                )
            }

            // Zoom In
            IconButton(
                onClick = { zoomLevel = (zoomLevel + 1.5f).coerceAtMost(22f) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("map_zoom_in_button")
            ) {
                Icon(
                    imageVector = WafaIcons.Add,
                    contentDescription = "Zoom In"
                )
            }

            // Zoom Out
            IconButton(
                onClick = { zoomLevel = (zoomLevel - 1.5f).coerceAtLeast(6f) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("map_zoom_out_button")
            ) {
                Icon(
                    imageVector = WafaIcons.Close,
                    contentDescription = "Zoom Out"
                )
            }
        }
    }
}

private fun DrawScope.drawVehicleBearingMarker(center: Offset, bearingDeg: Float) {
    val rad = Math.toRadians((bearingDeg - 90).toDouble())
    val length = 18.dp.toPx()
    val tipX = center.x + (length * cos(rad)).toFloat()
    val tipY = center.y + (length * sin(rad)).toFloat()

    val wingRad1 = Math.toRadians((bearingDeg + 145).toDouble())
    val wingRad2 = Math.toRadians((bearingDeg - 145).toDouble())
    val wingLen = 14.dp.toPx()

    val wing1 = Offset(
        center.x + (wingLen * cos(wingRad1)).toFloat(),
        center.y + (wingLen * sin(wingRad1)).toFloat()
    )
    val wing2 = Offset(
        center.x + (wingLen * cos(wingRad2)).toFloat(),
        center.y + (wingLen * sin(wingRad2)).toFloat()
    )

    val arrowPath = Path().apply {
        moveTo(tipX, tipY)
        lineTo(wing1.x, wing1.y)
        lineTo(center.x, center.y)
        lineTo(wing2.x, wing2.y)
        close()
    }

    drawPath(
        path = arrowPath,
        color = CyanNeon
    )
    drawCircle(
        color = Color.White,
        radius = 3.5.dp.toPx(),
        center = center
    )
}

private fun DrawScope.drawMapGrid(layer: MapLayerType, size: androidx.compose.ui.geometry.Size) {
    val gridSpacing = 48.dp.toPx()
    val gridColor = when (layer) {
        MapLayerType.STANDARD -> Color(0xFF1E293B).copy(alpha = 0.4f)
        MapLayerType.SATELLITE -> Color(0xFF0E2338).copy(alpha = 0.5f)
        MapLayerType.TERRAIN -> Color(0xFF233535).copy(alpha = 0.5f)
    }

    var x = 0f
    while (x < size.width) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += gridSpacing
    }

    var y = 0f
    while (y < size.height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += gridSpacing
    }
}
