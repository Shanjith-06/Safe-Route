package com.example.saferoute.ui.navigation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferoute.domain.model.HazardReport
import com.example.saferoute.domain.model.SafeHaven
import com.example.saferoute.domain.navigation.NavigationStatus
import com.example.saferoute.ui.components.InteractiveRouteMap
import com.example.saferoute.ui.navigation.components.RouteComparisonCard
import com.example.saferoute.ui.theme.DarkNavyCard
import com.example.saferoute.ui.theme.DarkNavySurface
import com.example.saferoute.ui.theme.PrimaryEmergency
import com.example.saferoute.ui.theme.SafeGreen
import com.example.saferoute.ui.theme.SecondaryPurple
import com.example.saferoute.ui.theme.WarningAmber

@Composable
fun SafeNavigationScreen(
    viewModel: SafeNavigationViewModel,
    safeHavens: List<SafeHaven> = emptyList(),
    hazards: List<HazardReport> = emptyList(),
    onTriggerSos: () -> Unit = {},
    onHavenNavigate: (SafeHaven) -> Unit = {}
) {
    val routes by viewModel.cachedRoutes.collectAsState()
    val selectedRouteId by viewModel.selectedRouteId.collectAsState()
    val navigationState by viewModel.navigationState.collectAsState()
    val safetyScoreResult by viewModel.selectedRouteSafetyScore.collectAsState()

    val originText by viewModel.originInput.collectAsState()
    val destText by viewModel.destinationInput.collectAsState()

    val isNavigating = navigationState.status == NavigationStatus.NAVIGATING ||
            navigationState.status == NavigationStatus.DEVIATION_DETECTED ||
            navigationState.status == NavigationStatus.UNEXPECTED_STOP_DETECTED

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavySurface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Search & Route Planning Header
            if (!isNavigating) {
                Surface(
                    color = DarkNavyCard,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Safe Route Navigation",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Dynamic safety score evaluation & live route monitoring",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Origin input
                        OutlinedTextField(
                            value = originText,
                            onValueChange = { viewModel.originInput.value = it },
                            label = { Text("Starting Point") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SafeGreen)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SafeGreen,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Destination input
                        OutlinedTextField(
                            value = destText,
                            onValueChange = { viewModel.destinationInput.value = it },
                            label = { Text("Destination") },
                            leadingIcon = {
                                Icon(Icons.Default.Place, contentDescription = null, tint = PrimaryEmergency)
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.calculateRoutes() }) {
                                    Icon(Icons.Default.Search, contentDescription = "Calculate", tint = SecondaryPurple)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryPurple,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = destText == "Home",
                                onClick = {
                                    viewModel.destinationInput.value = "Home"
                                    viewModel.calculateRoutes()
                                },
                                label = { Text("Home") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SecondaryPurple)
                            )
                            FilterChip(
                                selected = destText == "Work",
                                onClick = {
                                    viewModel.destinationInput.value = "Work"
                                    viewModel.calculateRoutes()
                                },
                                label = { Text("Work") },
                                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SecondaryPurple)
                            )
                            FilterChip(
                                selected = destText == "Campus",
                                onClick = {
                                    viewModel.destinationInput.value = "Campus"
                                    viewModel.calculateRoutes()
                                },
                                label = { Text("Campus") },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SecondaryPurple)
                            )
                        }
                    }
                }
            }

            // Interactive Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                InteractiveRouteMap(
                    routes = routes,
                    selectedRouteId = selectedRouteId,
                    safeHavens = safeHavens,
                    hazards = hazards,
                    userLat = if (isNavigating) navigationState.currentLat else 37.7749,
                    userLng = if (isNavigating) navigationState.currentLng else -122.4194,
                    onRouteSelected = { viewModel.selectRoute(it) },
                    onHavenNavigate = { onHavenNavigate(it) }
                )

                // Active Navigation HUD Header Overlay
                if (isNavigating) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
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
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = SafeGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Navigating to ${navigationState.activeRoute?.destinationName ?: "Destination"}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Speed: ${navigationState.currentSpeedKmH.toInt()} km/h • Remaining: ${(navigationState.distanceToDestinationMeters / 1000.0).let { "%.1f".format(it) }} km",
                                            color = SafeGreen,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                IconButton(onClick = { viewModel.stopNavigation() }) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = PrimaryEmergency)
                                }
                            }

                            // Demo simulation triggers bar
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.simulateDeviation() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber)
                                ) {
                                    Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Deviation", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.simulateUnexpectedStop() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryEmergency)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Test Stop", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Route Comparison Carousel / Navigation Launcher (when not navigating)
            if (!isNavigating) {
                Surface(
                    color = DarkNavyCard,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Safety Recommendation Banner
                        safetyScoreResult?.let { score ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when (score.riskLevel) {
                                        com.example.saferoute.domain.safety.RiskLevel.LOW -> SafeGreen.copy(alpha = 0.15f)
                                        com.example.saferoute.domain.safety.RiskLevel.MODERATE -> WarningAmber.copy(alpha = 0.15f)
                                        com.example.saferoute.domain.safety.RiskLevel.HIGH -> PrimaryEmergency.copy(alpha = 0.15f)
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = when (score.riskLevel) {
                                            com.example.saferoute.domain.safety.RiskLevel.LOW -> SafeGreen
                                            com.example.saferoute.domain.safety.RiskLevel.MODERATE -> WarningAmber
                                            com.example.saferoute.domain.safety.RiskLevel.HIGH -> PrimaryEmergency
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = score.summaryMessage,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Routes List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(routes, key = { it.id }) { route ->
                                RouteComparisonCard(
                                    route = route,
                                    isSelected = route.id == selectedRouteId,
                                    onSelect = { viewModel.selectRoute(route.id) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Launch Safe Navigation Button
                        Button(
                            onClick = { viewModel.startNavigation() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Safe Navigation",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Alert Dialog for Route Deviation or Unexpected Stop
        if (navigationState.status == NavigationStatus.DEVIATION_DETECTED ||
            navigationState.status == NavigationStatus.UNEXPECTED_STOP_DETECTED
        ) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissAlert() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = PrimaryEmergency,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = navigationState.alertTitle ?: "Safety Alert",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Text(
                        text = navigationState.alertMessage ?: "An unexpected route event was detected. Please verify your safety status.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    ElevatedButton(
                        onClick = { viewModel.dismissAlert() },
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = SafeGreen)
                    ) {
                        Text("I'm Safe", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.recalculateRoute() }) {
                            Text("Recalculate", color = SecondaryPurple)
                        }
                        Button(
                            onClick = {
                                viewModel.dismissAlert()
                                onTriggerSos()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmergency)
                        ) {
                            Text("SOS!", color = Color.White)
                        }
                    }
                },
                containerColor = DarkNavyCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
