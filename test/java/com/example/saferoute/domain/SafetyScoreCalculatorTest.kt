package com.example.saferoute.domain

import com.example.saferoute.domain.model.HazardReport
import com.example.saferoute.domain.model.SafeHaven
import com.example.saferoute.domain.safety.RiskLevel
import com.example.saferoute.domain.safety.SafetyScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyScoreCalculatorTest {

    @Test
    fun testWellLitRouteWithSafeHavensScoresHigh() {
        val havens = listOf(
            SafeHaven(id = 1, name = "Police", type = "Police Station", address = "A", latitude = 0.0, longitude = 0.0, isOpen24_7 = true),
            SafeHaven(id = 2, name = "Hospital", type = "Hospital", address = "B", latitude = 0.0, longitude = 0.0, isOpen24_7 = true)
        )
        val hazards = emptyList<HazardReport>()

        val result = SafetyScoreCalculator.calculateRouteSafetyScore(
            lightingLevel = "HIGH",
            nearbySafeHavens = havens,
            routeHazards = hazards,
            currentHourOfDay = 14
        )

        assertTrue("Safety score should be high (>=8.0)", result.score >= 8.0)
        assertEquals(RiskLevel.LOW, result.riskLevel)
    }

    @Test
    fun testUnlitRouteWithHazardsScoresLow() {
        val havens = emptyList<SafeHaven>()
        val hazards = listOf(
            HazardReport(id = 1, title = "Dark Alley", category = "Broken Lighting", description = "No light", latitude = 0.0, longitude = 0.0, severity = 3, status = "ACTIVE"),
            HazardReport(id = 2, title = "Harassment", category = "Suspicious Activity", description = "Unsafe", latitude = 0.0, longitude = 0.0, severity = 3, status = "ACTIVE")
        )

        val result = SafetyScoreCalculator.calculateRouteSafetyScore(
            lightingLevel = "LOW",
            nearbySafeHavens = havens,
            routeHazards = hazards,
            currentHourOfDay = 22
        )

        assertTrue("Safety score should be low (<5.5)", result.score < 5.5)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
    }
}
