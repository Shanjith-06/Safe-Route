package com.example.saferoute.ui.safehavens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferoute.domain.model.SafeHaven
import com.example.saferoute.ui.components.InteractiveRouteMap
import com.example.saferoute.ui.theme.DarkNavyCard
import com.example.saferoute.ui.theme.DarkNavySurface
import com.example.saferoute.ui.theme.SafeGreen
import com.example.saferoute.ui.theme.SecondaryPurple

@Composable
fun SafeHavensScreen(
    viewModel: SafeHavensViewModel,
    onNavigateToHaven: (SafeHaven) -> Unit = {}
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val havens by viewModel.filteredHavens.collectAsState()

    var isMapView by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavySurface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                color = DarkNavyCard,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Verified Safe Havens",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Police stations, hospitals & 24/7 refuge points",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        // Toggle List / Map View
                        IconButton(
                            onClick = { isMapView = !isMapView }
                        ) {
                            Icon(
                                imageVector = if (isMapView) Icons.Default.ViewList else Icons.Default.Map,
                                contentDescription = "Toggle View Mode",
                                tint = SecondaryPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search safe places or addresses...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
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

                    // Filter Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == HavenFilter.ALL,
                            onClick = { viewModel.selectedFilter.value = HavenFilter.ALL },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SecondaryPurple)
                        )
                        FilterChip(
                            selected = selectedFilter == HavenFilter.POLICE,
                            onClick = { viewModel.selectedFilter.value = HavenFilter.POLICE },
                            label = { Text("Police", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SecondaryPurple)
                        )
                        FilterChip(
                            selected = selectedFilter == HavenFilter.HOSPITAL,
                            onClick = { viewModel.selectedFilter.value = HavenFilter.HOSPITAL },
                            label = { Text("Hospitals", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SecondaryPurple)
                        )
                        FilterChip(
                            selected = selectedFilter == HavenFilter.STORE_24_7,
                            onClick = { viewModel.selectedFilter.value = HavenFilter.STORE_24_7 },
                            label = { Text("24/7 Stores", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SecondaryPurple)
                        )
                    }
                }
            }

            // Content Area: Map View or List View
            if (isMapView) {
                InteractiveRouteMap(
                    safeHavens = havens,
                    onHavenNavigate = { onNavigateToHaven(it) },
                    onHavenCall = { haven ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${haven.phoneNumber}"))
                        context.startActivity(intent)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(havens, key = { it.id }) { haven ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = SafeGreen.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        ) {
                                            Icon(
                                                imageVector = when (haven.type) {
                                                    "Police Station" -> Icons.Default.Shield
                                                    "Hospital" -> Icons.Default.LocalHospital
                                                    "24/7 Store" -> Icons.Default.Store
                                                    else -> Icons.Default.Park
                                                },
                                                contentDescription = null,
                                                tint = SafeGreen,
                                                modifier = Modifier
                                                    .padding(10.dp)
                                                    .size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = haven.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 16.sp
                                                )
                                                if (haven.isVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Verified",
                                                        tint = SafeGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${haven.type} • ${haven.distanceMeters.toInt()} meters away",
                                                color = SafeGreen,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    if (haven.isOpen24_7) {
                                        Surface(
                                            color = Color(0xFF052E16),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "OPEN 24/7",
                                                color = SafeGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = haven.address,
                                        color = Color.LightGray,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (haven.phoneNumber.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${haven.phoneNumber}"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call", fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { onNavigateToHaven(haven) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple)
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Route Here", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Haven FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = SafeGreen,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Safe Haven")
        }

        // Add Custom Safe Haven Dialog
        if (showAddDialog) {
            var name by remember { mutableStateOf("") }
            var type by remember { mutableStateOf("24/7 Store") }
            var address by remember { mutableStateOf("") }
            var phone by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text("Add Safe Haven Spot", fontWeight = FontWeight.Bold, color = Color.White)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Haven Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addCustomHaven(name, type, address, phone, 37.7750, -122.4190)
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                    ) {
                        Text("Add Spot")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = DarkNavyCard
            )
        }
    }
}
