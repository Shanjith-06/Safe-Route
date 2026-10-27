package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journeys")
data class JourneyEntity(
    @PrimaryKey
    val journeyId: String,
    val startLatitude: Double,
    val startLongitude: Double,
    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val chosenRouteType: String = "SAFEST", // SAFEST, SHORTEST, BALANCED
    val safetyScore: Double = 8.5,
    val distanceMeters: Double = 0.0,
    val durationMinutes: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val status: String = "IN_PROGRESS", // IN_PROGRESS, COMPLETED, EMERGENCY_TRIGGERED, CANCELLED
    val deviationsCount: Int = 0,
    val unexpectedStopsCount: Int = 0,
    val isSynced: Boolean = false
)
