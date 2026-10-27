package com.example.saferoute.domain

import com.example.saferoute.domain.model.SafetyRoute
import com.example.saferoute.domain.navigation.NavigationMonitor
import com.example.saferoute.domain.navigation.NavigationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationMonitorTest {

    @Test
    fun testRouteDeviationDetection() {
        val monitor = NavigationMonitor()
        val route = SafetyRoute(
            id = 1,
            originName = "Start",
            destinationName = "End",
            originLat = 37.7749,
            originLng = -122.4194,
            destLat = 37.7833,
            destLng = -122.4167,
            distanceKm = 1.0,
            estimatedMinutes = 10,
            safetyScore = 9.0,
            lightingLevel = "HIGH",
            openFacilitiesCount = 2,
            riskRating = "LOW"
        )

        monitor.startNavigation(route)
        assertEquals(NavigationStatus.NAVIGATING, monitor.state.value.status)

        // Simulate position far away from origin/dest
        monitor.updateLocation(37.7800, -122.4100, 15.0)

        assertEquals(NavigationStatus.DEVIATION_DETECTED, monitor.state.value.status)
    }
}
