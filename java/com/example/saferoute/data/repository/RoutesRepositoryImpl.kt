package com.example.saferoute.data.repository

import com.example.saferoute.data.local.db.dao.CachedRouteDao
import com.example.saferoute.data.local.db.entity.CachedRouteEntity
import com.example.saferoute.domain.model.RoutePoint
import com.example.saferoute.domain.model.SafetyRoute
import com.example.saferoute.domain.repository.RoutesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.min

class RoutesRepositoryImpl(
    private val dao: CachedRouteDao
) : RoutesRepository {

    override fun getCachedRoutes(): Flow<List<SafetyRoute>> {
        return dao.getAllRoutes().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun calculateSafetyRoutes(
        originName: String,
        destinationName: String,
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): List<SafetyRoute> {
        val calculated = listOf(
            SafetyRoute(
                id = 101,
                originName = originName,
                destinationName = destinationName,
                originLat = originLat,
                originLng = originLng,
                destLat = destLat,
                destLng = destLng,
                distanceKm = 1.6,
                estimatedMinutes = 19,
                safetyScore = 9.4,
                lightingLevel = "HIGH",
                openFacilitiesCount = 6,
                riskRating = "LOW",
                isRecommended = true,
                waypoints = generateWaypoints(originLat, originLng, destLat, destLng, variant = 0)
            ),
            SafetyRoute(
                id = 102,
                originName = originName,
                destinationName = destinationName,
                originLat = originLat,
                originLng = originLng,
                destLat = destLat,
                destLng = destLng,
                distanceKm = 1.3,
                estimatedMinutes = 15,
                safetyScore = 7.1,
                lightingLevel = "MEDIUM",
                openFacilitiesCount = 2,
                riskRating = "MODERATE",
                isRecommended = false,
                waypoints = generateWaypoints(originLat, originLng, destLat, destLng, variant = 1)
            ),
            SafetyRoute(
                id = 103,
                originName = originName,
                destinationName = destinationName,
                originLat = originLat,
                originLng = originLng,
                destLat = destLat,
                destLng = destLng,
                distanceKm = 1.0,
                estimatedMinutes = 12,
                safetyScore = 4.8,
                lightingLevel = "LOW",
                openFacilitiesCount = 0,
                riskRating = "HIGH",
                isRecommended = false,
                waypoints = generateWaypoints(originLat, originLng, destLat, destLng, variant = 2)
            )
        )

        // Cache into DB
        dao.clearRoutes()
        dao.insertRoutes(calculated.map { it.toEntity() })

        return calculated
    }

    override fun evaluateSafetyScore(lightingLevel: String, openFacilities: Int, hazardCount: Int): Double {
        var base = 5.0
        when (lightingLevel.uppercase()) {
            "HIGH" -> base += 3.0
            "MEDIUM" -> base += 1.5
            "LOW" -> base -= 1.0
        }
        base += min(openFacilities * 0.5, 2.0)
        base -= min(hazardCount * 1.5, 4.0)
        return max(1.0, min(10.0, base))
    }

    private fun generateWaypoints(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double,
        variant: Int
    ): List<RoutePoint> {
        val midLat = (startLat + endLat) / 2.0 + (variant * 0.002)
        val midLng = (startLng + endLng) / 2.0 + (variant * -0.002)
        return listOf(
            RoutePoint(startLat, startLng, "Start"),
            RoutePoint(midLat, midLng, "Safe Haven Waypoint"),
            RoutePoint(endLat, endLng, "Destination")
        )
    }

    private fun CachedRouteEntity.toDomainModel() = SafetyRoute(
        id = id,
        originName = originName,
        destinationName = destinationName,
        originLat = originLat,
        originLng = originLng,
        destLat = destLat,
        destLng = destLng,
        distanceKm = distanceKm,
        estimatedMinutes = estimatedMinutes,
        safetyScore = safetyScore,
        lightingLevel = lightingLevel,
        openFacilitiesCount = openFacilitiesCount,
        riskRating = riskRating,
        isRecommended = isRecommended,
        waypoints = emptyList()
    )

    private fun SafetyRoute.toEntity() = CachedRouteEntity(
        id = id,
        originName = originName,
        destinationName = destinationName,
        originLat = originLat,
        originLng = originLng,
        destLat = destLat,
        destLng = destLng,
        distanceKm = distanceKm,
        estimatedMinutes = estimatedMinutes,
        safetyScore = safetyScore,
        lightingLevel = lightingLevel,
        openFacilitiesCount = openFacilitiesCount,
        riskRating = riskRating,
        isRecommended = isRecommended,
        routeWaypointsJson = ""
    )
}
