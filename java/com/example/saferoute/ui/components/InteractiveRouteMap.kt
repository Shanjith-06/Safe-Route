package com.example.saferoute.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferoute.domain.model.HazardReport
import com.example.saferoute.domain.model.SafeHaven
import com.example.saferoute.domain.model.SafetyRoute
import com.example.saferoute.ui.theme.DarkNavyCard
import com.example.saferoute.ui.theme.DarkNavySurface
import com.example.saferoute.ui.theme.PrimaryEmergency
import com.example.saferoute.ui.theme.SafeGreen
import com.example.saferoute.ui.theme.SecondaryPurple
import com.example.saferoute.ui.theme.WarningAmber

sealed class SelectedMapMarker {
    data class HavenMarker(val haven: SafeHaven) : SelectedMapMarker()
    data class HazardMarker(val hazard: HazardReport) : SelectedMapMarker()
}

@Composable
fun InteractiveRouteMap(
    modifier: Modifier = Modifier,
    routes: List<SafetyRoute> = emptyList(),
    selectedRouteId: Long? = null,
    safeHavens: List<SafeHaven> = emptyList(),
    hazards: List<HazardReport> = emptyList(),
    userLat: Double = 37.7749,
    userLng: Double = -122.4194,
    onRouteSelected: (Long) -> Unit = {},
    onHavenNavigate: (SafeHaven) -> Unit = {},
    onHavenCall: (SafeHaven) -> Unit = {}
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    var showHavensLayer by remember { mutableStateOf(true) }
    var showHazardsLayer by remember { mutableStateOf(true) }
    var selectedMarker by remember { mutableStateOf<SelectedMapMarker?>(null) }

    // Pulse animation for user position
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by transition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRadius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavySurface)
    ) {
        // Map Canvas Rendering
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.0f)
                        panOffset += pan
                    }
                }
                .pointerInput(safeHavens, hazards) {
                    detectTapGestures { tapOffset ->
                        // Interactive tap hit testing for markers
                        // Scale/pan map center projection
                        val width = size.width
                        val height = size.height
                        val center = Offset(width / 2f, height / 2f) + panOffset

                        var clicked: SelectedMapMarker? = null

                        if (showHavensLayer) {
                            for (haven in safeHavens) {
                                val x = center.x + ((haven.longitude - userLng) * 18000 * zoomScale).toFloat()
                                val y = center.y - ((haven.latitude - userLat) * 18000 * zoomScale).toFloat()
                                val dist = (tapOffset - Offset(x, y)).getDistance()
                                if (dist < 40f) {
                                    clicked = SelectedMapMarker.HavenMarker(haven)
                                    break
                                }
                            }
                        }

                        if (clicked == null && showHazardsLayer) {
                            for (hazard in hazards) {
                                val x = center.x + ((hazard.longitude - userLng) * 18000 * zoomScale).toFloat()
                                val y = center.y - ((hazard.latitude - userLat) * 18000 * zoomScale).toFloat()
                                val dist = (tapOffset - Offset(x, y)).getDistance()
                                if (dist < 40f) {
                                    clicked = SelectedMapMarker.HazardMarker(hazard)
                                    break
                                }
                            }
                        }

                        selectedMarker = clicked
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val mapCenter = Offset(width / 2f, height / 2f) + panOffset

            // 1. Draw Grid lines (Simulated Street Network)
            val gridStep = 80f * zoomScale
            var gx = (mapCenter.x % gridStep)
            while (gx < width) {
                drawLine(
                    color = Color(0xFF1E2A45),
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 1.5f
                )
                gx += gridStep
            }
            var gy = (mapCenter.y % gridStep)
            while (gy < height) {
                drawLine(
                    color = Color(0xFF1E2A45),
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1.5f
                )
                gy += gridStep
            }

            // Function to map lat/lng to canvas offset
            fun project(lat: Double, lng: Double): Offset {
                val dx = ((lng - userLng) * 18000 * zoomScale).toFloat()
                val dy = -((lat - userLat) * 18000 * zoomScale).toFloat()
                return mapCenter + Offset(dx, dy)
            }

            // 2. Draw Safety Corridors / Routes
            routes.forEach { route ->
                val isSelected = route.id == selectedRouteId
                val routeColor = when {
                    route.safetyScore >= 8.0 -> SafeGreen
                    route.safetyScore >= 5.5 -> WarningAmber
                    else -> PrimaryEmergency
                }

                val strokeWidth = if (isSelected) 10f * zoomScale else 5f * zoomScale
                val start = project(route.originLat, route.originLng)
                val end = project(route.destLat, route.destLng)

                val path = Path().apply {
                    moveTo(start.x, start.y)
                    if (route.waypoints.isNotEmpty()) {
                        route.waypoints.forEach { wp ->
                            val pt = project(wp.latitude, wp.longitude)
                            lineTo(pt.x, pt.y)
                        }
                    } else {
                        // Curved control point
                        val control = Offset((start.x + end.x) / 2f + 40f, (start.y + end.y) / 2f - 40f)
                        quadraticTo(control.x, control.y, end.x, end.y)
                    }
                }

                // Route shadow/glow
                drawPath(
                    path = path,
                    color = routeColor.copy(alpha = if (isSelected) 0.3f else 0.15f),
                    style = Stroke(width = strokeWidth + 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Route main line
                drawPath(
                    path = path,
                    color = if (isSelected) routeColor else routeColor.copy(alpha = 0.6f),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = if (!isSelected) PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f) else null
                    )
                )

                // Destination Pin
                drawCircle(
                    color = PrimaryEmergency,
                    radius = 12f * zoomScale,
                    center = end
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f * zoomScale,
                    center = end
                )
            }

            // 3. Draw Safe Haven Markers
            if (showHavensLayer) {
                safeHavens.forEach { haven ->
                    val pos = project(haven.latitude, haven.longitude)
                    drawCircle(
                        color = Color(0xFF003642),
                        radius = 16f * zoomScale,
                        center = pos
                    )
                    drawCircle(
                        color = SafeGreen,
                        radius = 12f * zoomScale,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f * zoomScale,
                        center = pos
                    )
                }
            }

            // 4. Draw Hazard Report Markers
            if (showHazardsLayer) {
                hazards.forEach { hazard ->
                    val pos = project(hazard.latitude, hazard.longitude)
                    drawCircle(
                        color = Color(0xFF451A03),
                        radius = 14f * zoomScale,
                        center = pos
                    )
                    drawCircle(
                        color = WarningAmber,
                        radius = 10f * zoomScale,
                        center = pos
                    )
                }
            }

            // 5. Draw Current User Position (Pulsing Dot)
            val userPos = project(userLat, userLng)

            // Pulse outer halo
            drawCircle(
                color = SecondaryPurple.copy(alpha = 0.35f),
                radius = pulseRadius * zoomScale,
                center = userPos
            )

            // User core pin
            drawCircle(
                color = Color.White,
                radius = 10f * zoomScale,
                center = userPos
            )
            drawCircle(
                color = SecondaryPurple,
                radius = 7f * zoomScale,
                center = userPos
            )
        }

        // Layer Filter Chips Overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showHavensLayer,
                onClick = { showHavensLayer = !showHavensLayer },
                label = { Text("Safe Havens", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SafeGreen.copy(alpha = 0.2f),
                    selectedLabelColor = SafeGreen
                )
            )

            FilterChip(
                selected = showHazardsLayer,
                onClick = { showHazardsLayer = !showHazardsLayer },
                label = { Text("Hazards", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarningAmber.copy(alpha = 0.2f),
                    selectedLabelColor = WarningAmber
                )
            )
        }

        // Map Control Buttons (Zoom In/Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = DarkNavyCard,
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.0f) }) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
                }
            }

            Surface(
                shape = CircleShape,
                color = DarkNavyCard,
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
                }
            }

            Surface(
                shape = CircleShape,
                color = DarkNavyCard,
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = {
                    panOffset = Offset.Zero
                    zoomScale = 1.0f
                }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = SecondaryPurple)
                }
            }
        }

        // Selected Marker Details Popup Card
        selectedMarker?.let { marker ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (marker) {
                    is SelectedMapMarker.HavenMarker -> {
                        val haven = marker.haven
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (haven.type) {
                                                "Police Station" -> Icons.Default.Shield
                                                "Hospital" -> Icons.Default.LocalHospital
                                                "24/7 Store" -> Icons.Default.Store
                                                else -> Icons.Default.Park
                                            },
                                            contentDescription = null,
                                            tint = SafeGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = haven.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "${haven.type} • ${haven.distanceMeters.toInt()}m away",
                                                color = SafeGreen,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    IconButton(onClick = { selectedMarker = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = haven.address,
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (haven.phoneNumber.isNotEmpty()) {
                                        TextButton(onClick = { onHavenCall(haven) }) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call")
                                        }
                                    }
                                    TextButton(onClick = { onHavenNavigate(haven) }) {
                                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Route Here")
                                    }
                                }
                            }
                        }
                    }

                    is SelectedMapMarker.HazardMarker -> {
                        val hazard = marker.hazard
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = WarningAmber,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = hazard.title,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = "Category: ${hazard.category} • Severity: Level ${hazard.severity}",
                                                color = WarningAmber,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    IconButton(onClick = { selectedMarker = null }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = hazard.description,
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
