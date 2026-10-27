package com.example.saferoute.domain.navigation

import com.example.saferoute.domain.model.RoutePoint
import com.example.saferoute.domain.model.SafetyRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class NavigationStatus {
    IDLE,
    NAVIGATING,
    DEVIATION_DETECTED,
    UNEXPECTED_STOP_DETECTED,
    ARRIVED
}

data class NavigationState(
    val status: NavigationStatus = NavigationStatus.IDLE,
    val activeRoute: SafetyRoute? = null,
    val currentLat: Double = 0.0,
    val currentLng: Double = 0.0,
    val currentSpeedKmH: Double = 0.0,
    val distanceToDestinationMeters: Double = 0.0,
    val deviationMeters: Double = 0.0,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val isSimulated: Boolean = true
)

class NavigationMonitor {

    private val _state = MutableStateFlow(NavigationState())
    val state: StateFlow<NavigationState> = _state.asStateFlow()

    private var stopStartTimeMillis: Long = 0L
    private val DEVIATION_THRESHOLD_METERS = 40.0 // 30-50m GPS tolerance requirement
    private val STOP_DURATION_THRESHOLD_MILLIS = 15000L // 15 seconds stop threshold

    fun startNavigation(route: SafetyRoute, isSimulated: Boolean = true) {
        _state.value = NavigationState(
            status = NavigationStatus.NAVIGATING,
            activeRoute = route,
            currentLat = route.originLat,
            currentLng = route.originLng,
            currentSpeedKmH = 25.0,
            distanceToDestinationMeters = route.distanceKm * 1000.0,
            deviationMeters = 0.0,
            isSimulated = isSimulated
        )
        stopStartTimeMillis = 0L
    }

    fun stopNavigation() {
        _state.value = NavigationState(status = NavigationStatus.IDLE)
        stopStartTimeMillis = 0L
    }

    fun updateLocation(lat: Double, lng: Double, speedKmH: Double) {
        val currentRoute = _state.value.activeRoute ?: return
        if (_state.value.status == NavigationStatus.IDLE || _state.value.status == NavigationStatus.ARRIVED) return

        val distanceToDest = calculateDistanceMeters(lat, lng, currentRoute.destLat, currentRoute.destLng)

        // Arrived check
        if (distanceToDest < 20.0) {
            _state.value = _state.value.copy(
                status = NavigationStatus.ARRIVED,
                currentLat = lat,
                currentLng = lng,
                distanceToDestinationMeters = 0.0,
                alertTitle = "Destination Reached",
                alertMessage = "You have safely arrived at ${currentRoute.destinationName}."
            )
            return
        }

        // Calculate deviation from route segment/waypoints
        val deviation = calculateRouteDeviationMeters(lat, lng, currentRoute)

        // Check for Route Deviation
        if (deviation > DEVIATION_THRESHOLD_METERS && _state.value.status != NavigationStatus.DEVIATION_DETECTED) {
            _state.value = _state.value.copy(
                status = NavigationStatus.DEVIATION_DETECTED,
                currentLat = lat,
                currentLng = lng,
                deviationMeters = deviation,
                currentSpeedKmH = speedKmH,
                distanceToDestinationMeters = distanceToDest,
                alertTitle = "Route Deviation Detected",
                alertMessage = "You are ${deviation.toInt()}m off your calculated safe route. Are you safe?"
            )
            return
        }

        // Check for Unexpected Stop
        if (speedKmH < 1.5 && distanceToDest > 50.0) {
            if (stopStartTimeMillis == 0L) {
                stopStartTimeMillis = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - stopStartTimeMillis > STOP_DURATION_THRESHOLD_MILLIS &&
                _state.value.status != NavigationStatus.UNEXPECTED_STOP_DETECTED
            ) {
                _state.value = _state.value.copy(
                    status = NavigationStatus.UNEXPECTED_STOP_DETECTED,
                    currentLat = lat,
                    currentLng = lng,
                    deviationMeters = deviation,
                    currentSpeedKmH = speedKmH,
                    distanceToDestinationMeters = distanceToDest,
                    alertTitle = "Unexpected Stop Detected",
                    alertMessage = "You have been stopped for over 15 seconds away from safe havens. Please confirm your safety."
                )
                return
            }
        } else {
            stopStartTimeMillis = 0L
        }

        // Normal Navigating State update
        if (_state.value.status == NavigationStatus.NAVIGATING) {
            _state.value = _state.value.copy(
                currentLat = lat,
                currentLng = lng,
                currentSpeedKmH = speedKmH,
                distanceToDestinationMeters = distanceToDest,
                deviationMeters = deviation
            )
        }
    }

    fun dismissAlertAndContinue() {
        stopStartTimeMillis = 0L
        _state.value = _state.value.copy(
            status = NavigationStatus.NAVIGATING,
            alertTitle = null,
            alertMessage = null
        )
    }

    fun triggerRecalculate() {
        stopStartTimeMillis = 0L
        val currentRoute = _state.value.activeRoute ?: return
        // Adjust active route from current location
        val recalculatedRoute = currentRoute.copy(
            originLat = _state.value.currentLat,
            originLng = _state.value.currentLng,
            originName = "Current Rerouted Position",
            distanceKm = maxOf(0.2, _state.value.distanceToDestinationMeters / 1000.0),
            safetyScore = minOf(9.8, currentRoute.safetyScore + 0.2)
        )
        _state.value = _state.value.copy(
            status = NavigationStatus.NAVIGATING,
            activeRoute = recalculatedRoute,
            alertTitle = "Route Recalculated",
            alertMessage = "New safe route calculated from your position."
        )
    }

    private fun calculateRouteDeviationMeters(lat: Double, lng: Double, route: SafetyRoute): Double {
        if (route.waypoints.isEmpty()) {
            // Direct line calculation
            val distStart = calculateDistanceMeters(lat, lng, route.originLat, route.originLng)
            val distEnd = calculateDistanceMeters(lat, lng, route.destLat, route.destLng)
            val totalDist = calculateDistanceMeters(route.originLat, route.originLng, route.destLat, route.destLng)
            return maxOf(0.0, (distStart + distEnd - totalDist) * 0.5)
        }
        var minDistance = Double.MAX_VALUE
        for (wp in route.waypoints) {
            val d = calculateDistanceMeters(lat, lng, wp.latitude, wp.longitude)
            if (d < minDistance) minDistance = d
        }
        return minDistance
    }

    companion object {
        fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371000.0 // Earth radius in meters
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }
    }
}
