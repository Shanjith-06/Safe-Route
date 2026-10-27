package com.example.saferoute.ui.hazards

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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saferoute.ui.theme.DarkNavyCard
import com.example.saferoute.ui.theme.DarkNavySurface
import com.example.saferoute.ui.theme.PrimaryEmergency
import com.example.saferoute.ui.theme.SafeGreen
import com.example.saferoute.ui.theme.SecondaryPurple
import com.example.saferoute.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HazardReportScreen(
    viewModel: HazardsViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val hazards by viewModel.filteredHazards.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }

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
                    Text(
                        text = "Community Hazard Reports",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Crowdsourced safety issues logged with offline persistence",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search reported hazards...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarningAmber,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Filter Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == "ALL",
                            onClick = { viewModel.selectedCategory.value = "ALL" },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarningAmber)
                        )
                        FilterChip(
                            selected = selectedCategory == "Broken Lighting",
                            onClick = { viewModel.selectedCategory.value = "Broken Lighting" },
                            label = { Text("Lighting", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarningAmber)
                        )
                        FilterChip(
                            selected = selectedCategory == "Suspicious Activity",
                            onClick = { viewModel.selectedCategory.value = "Suspicious Activity" },
                            label = { Text("Suspicious", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarningAmber)
                        )
                    }
                }
            }

            // Hazard Reports List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(hazards, key = { it.id }) { hazard ->
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
                                        color = when (hazard.severity) {
                                            1 -> WarningAmber.copy(alpha = 0.2f)
                                            2 -> WarningAmber.copy(alpha = 0.3f)
                                            else -> PrimaryEmergency.copy(alpha = 0.3f)
                                        },
                                        shape = CircleShape
                                    ) {
                                        Icon(
                                            imageVector = when (hazard.category) {
                                                "Broken Lighting" -> Icons.Default.Lightbulb
                                                "Safe Zone" -> Icons.Default.Shield
                                                else -> Icons.Default.Warning
                                            },
                                            contentDescription = null,
                                            tint = if (hazard.severity >= 3) PrimaryEmergency else WarningAmber,
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

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

                                // Sync status tag
                                Surface(
                                    color = if (hazard.isSynced) SafeGreen.copy(alpha = 0.15f) else Color(0xFF334155),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (hazard.isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                            contentDescription = null,
                                            tint = if (hazard.isSynced) SafeGreen else Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (hazard.isSynced) "Synced" else "Offline Cache",
                                            color = if (hazard.isSynced) SafeGreen else Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = hazard.description,
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(hazard.timestamp))
                                Text(
                                    text = "Reported: $dateStr",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )

                                IconButton(
                                    onClick = { viewModel.deleteHazard(hazard.id) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Report Hazard FAB
        FloatingActionButton(
            onClick = { showReportDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = WarningAmber,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Report Hazard")
        }

        // Report Hazard Modal Dialog
        if (showReportDialog) {
            var title by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("Broken Lighting") }
            var description by remember { mutableStateOf("") }
            var severity by remember { mutableFloatStateOf(2.0f) }
            var expandedDropdown by remember { mutableStateOf(false) }

            val categories = listOf(
                "Broken Lighting",
                "Unsafe Area",
                "Suspicious Activity",
                "Road Block",
                "Police Checkpoint",
                "Safe Zone"
            )

            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Report, contentDescription = null, tint = WarningAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Safety Incident", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (e.g. Unlit Walkway)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown,
                            onExpandedChange = { expandedDropdown = !expandedDropdown }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            expandedDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description & Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Column {
                            Text(
                                text = "Severity Level: ${severity.toInt()} (${when (severity.toInt()) { 1 -> "Low"; 2 -> "Medium"; else -> "High Risk" }})",
                                fontSize = 12.sp,
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = severity,
                                onValueChange = { severity = it },
                                valueRange = 1.0f..3.0f,
                                steps = 1,
                                colors = SliderDefaults.colors(
                                    thumbColor = WarningAmber,
                                    activeTrackColor = WarningAmber
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.reportHazard(
                                    title = title,
                                    category = category,
                                    description = description,
                                    severity = severity.toInt()
                                )
                                showReportDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                    ) {
                        Text("Submit Report", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = DarkNavyCard
            )
        }
    }
}
