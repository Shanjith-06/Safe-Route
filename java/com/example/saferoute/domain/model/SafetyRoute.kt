package com.example.saferoute.domain.model

data class SafetyRoute(
    val id: Long = 0,
    val originName: String,
    val destinationName: String,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val safetyScore: Double,
    val lightingLevel: String,
    val openFacilitiesCount: Int,
    val riskRating: String,
    val isRecommended: Boolean = true,
    val waypoints: List<RoutePoint> = emptyList()
)

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
)
