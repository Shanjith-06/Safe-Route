package com.example.saferoute.domain.repository

import com.example.saferoute.domain.model.SafetyRoute
import kotlinx.coroutines.flow.Flow

interface RoutesRepository {
    fun getCachedRoutes(): Flow<List<SafetyRoute>>
    suspend fun calculateSafetyRoutes(
        originName: String,
        destinationName: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): List<SafetyRoute>
    fun evaluateSafetyScore(lightingLevel: String, openFacilities: Int, hazardCount: Int): Double
}
