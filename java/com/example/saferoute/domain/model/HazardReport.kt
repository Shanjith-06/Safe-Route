package com.example.saferoute.domain.model

data class HazardReport(
    val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val severity: Int = 2,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val status: String = "ACTIVE"
)
