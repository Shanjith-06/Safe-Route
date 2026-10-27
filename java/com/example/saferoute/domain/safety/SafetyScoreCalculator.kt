package com.example.saferoute.domain.safety

import com.example.saferoute.data.local.db.entity.SafetyReportEntity
import com.example.saferoute.domain.model.HazardReport
import com.example.saferoute.domain.model.SafeHaven
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class RiskLevel {
    LOW,
    MODERATE,
    HIGH
}

data class SafetyWeights(
    val lightingWeight: Double = 0.25,
    val incidentSafetyWeight: Double = 0.20,
    val commercialActivityWeight: Double = 0.15,
    val safeFacilitiesWeight: Double = 0.15,
    val communityReportsWeight: Double = 0.15,
    val timeOfDayWeight: Double = 0.10
)

data class FactorBreakdown(
    val factorName: String,
    val weightPercent: Int,
    val rawScore: Double, // 1.0 to 10.0
    val weightedContribution: Double,
    val detail: String
)

data class SafetyScoreResult(
    val score: Double, // 1.0 to 10.0
    val riskLevel: RiskLevel,
    val lightingScore: Double,
    val incidentSafetyScore: Double,
    val commercialActivityScore: Double,
    val safeFacilitiesScore: Double,
    val communityReportsScore: Double,
    val timeOfDayScore: Double,
    val factorBreakdowns: List<FactorBreakdown>,
    val summaryMessage: String,
    val recommendations: List<String>
)

object SafetyScoreCalculator {

    val defaultWeights = SafetyWeights()

    fun calculateDeterministicSafetyScore(
        lightingLevel: String, // "HIGH", "MEDIUM", "LOW"
        nearbySafeHavens: List<SafeHaven>,
        communityReports: List<SafetyReportEntity> = emptyList(),
        commercialActivityLevel: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
        currentHourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        weights: SafetyWeights = defaultWeights
    ): SafetyScoreResult {
        // 1. Lighting factor (25% weight)
        val lightingScore = when (lightingLevel.uppercase()) {
            "HIGH" -> 9.5
            "MEDIUM" -> 7.0
            "LOW" -> 3.5
            else -> 6.0
        }

        // 2. Incident safety factor (20% weight) - inverse of historical/critical incidents
        val highSeverityIncidents = communityReports.count { it.severity >= 3 && it.status == "ACTIVE" }
        val incidentSafetyScore = max(1.0, min(10.0, 9.5 - (highSeverityIncidents * 2.5)))

        // 3. Commercial activity factor (15% weight) - street vibrancy and foot traffic
        val commercialScore = when (commercialActivityLevel.uppercase()) {
            "HIGH" -> 9.0
            "MEDIUM" -> 7.5
            "LOW" -> 4.0
            else -> 6.5
        }

        // 4. Safe facilities factor (15% weight) - presence of police, hospitals, 24/7 stores
        val policeCount = nearbySafeHavens.count { it.type.contains("Police", ignoreCase = true) }
        val open247Count = nearbySafeHavens.count { it.isOpen24_7 }
        val facilitiesScore = max(2.0, min(10.0, 4.0 + (policeCount * 2.5) + (open247Count * 1.2)))

        // 5. Community reports factor (15% weight) - broken street lights, suspicious activity
        val activeReportsCount = communityReports.count { it.status == "ACTIVE" }
        val communityReportsScore = max(1.5, min(10.0, 9.5 - (activeReportsCount * 1.5)))

        // 6. Time of day factor (10% weight)
        // 06:00 to 18:00 (daytime): highest baseline
        // 18:00 to 22:00 (evening): moderate
        // 22:00 to 05:00 (late night): lowest baseline
        val timeOfDayScore = when {
            currentHourOfDay in 6..17 -> 9.5
            currentHourOfDay in 18..21 -> 7.0
            currentHourOfDay in 22..23 || currentHourOfDay in 0..4 -> 3.5
            else -> 6.0
        }

        // Weighted total calculation
        val totalWeightedScore = (lightingScore * weights.lightingWeight) +
                (incidentSafetyScore * weights.incidentSafetyWeight) +
                (commercialScore * weights.commercialActivityWeight) +
                (facilitiesScore * weights.safeFacilitiesWeight) +
                (communityReportsScore * weights.communityReportsWeight) +
                (timeOfDayScore * weights.timeOfDayWeight)

        val clampedScore = ((totalWeightedScore * 10.0).roundToInt() / 10.0).coerceIn(1.0, 10.0)

        val riskLevel = when {
            clampedScore >= 8.0 -> RiskLevel.LOW
            clampedScore >= 5.5 -> RiskLevel.MODERATE
            else -> RiskLevel.HIGH
        }

        val factorBreakdowns = listOf(
            FactorBreakdown(
                factorName = "Street Lighting",
                weightPercent = (weights.lightingWeight * 100).toInt(),
                rawScore = lightingScore,
                weightedContribution = lightingScore * weights.lightingWeight,
                detail = "$lightingLevel level illumination along route corridor"
            ),
            FactorBreakdown(
                factorName = "Incident Safety History",
                weightPercent = (weights.incidentSafetyWeight * 100).toInt(),
                rawScore = incidentSafetyScore,
                weightedContribution = incidentSafetyScore * weights.incidentSafetyWeight,
                detail = "$highSeverityIncidents severe incidents logged in area"
            ),
            FactorBreakdown(
                factorName = "Commercial Activity",
                weightPercent = (weights.commercialActivityWeight * 100).toInt(),
                rawScore = commercialScore,
                weightedContribution = commercialScore * weights.commercialActivityWeight,
                detail = "$commercialActivityLevel foot traffic & open storefronts"
            ),
            FactorBreakdown(
                factorName = "Nearby Safe Havens",
                weightPercent = (weights.safeFacilitiesWeight * 100).toInt(),
                rawScore = facilitiesScore,
                weightedContribution = facilitiesScore * weights.safeFacilitiesWeight,
                detail = "$policeCount police stations & $open247Count 24/7 facilities"
            ),
            FactorBreakdown(
                factorName = "Community Reports",
                weightPercent = (weights.communityReportsWeight * 100).toInt(),
                rawScore = communityReportsScore,
                weightedContribution = communityReportsScore * weights.communityReportsWeight,
                detail = "$activeReportsCount active community reports nearby"
            ),
            FactorBreakdown(
                factorName = "Time of Day",
                weightPercent = (weights.timeOfDayWeight * 100).toInt(),
                rawScore = timeOfDayScore,
                weightedContribution = timeOfDayScore * weights.timeOfDayWeight,
                detail = "Assessed at ${String.format("%02d:00", currentHourOfDay)}"
            )
        )

        val recommendations = mutableListOf<String>()
        if (lightingScore < 6.0) recommendations.add("Low lighting along stretches; avoid unlit alleyways.")
        if (facilitiesScore >= 7.0) recommendations.add("Multiple 24/7 safe havens along this route.")
        if (communityReportsScore < 6.0) recommendations.add("Active hazard reports submitted near path.")
        if (timeOfDayScore < 5.0) recommendations.add("Late night travel: Keep live tracking active.")

        val summaryMessage = when (riskLevel) {
            RiskLevel.LOW -> "Safe Route: Well-illuminated thoroughfare with active commercial presence and nearby police support."
            RiskLevel.MODERATE -> "Moderate Safety: Standard residential route. Minor hazard reports present."
            RiskLevel.HIGH -> "High Risk Warning: Low lighting or isolated stretches detected. Safer alternative strongly recommended."
        }

        return SafetyScoreResult(
            score = clampedScore,
            riskLevel = riskLevel,
            lightingScore = lightingScore,
            incidentSafetyScore = incidentSafetyScore,
            commercialActivityScore = commercialScore,
            safeFacilitiesScore = facilitiesScore,
            communityReportsScore = communityReportsScore,
            timeOfDayScore = timeOfDayScore,
            factorBreakdowns = factorBreakdowns,
            summaryMessage = summaryMessage,
            recommendations = recommendations
        )
    }

    // Backward-compatible signature
    fun calculateRouteSafetyScore(
        lightingLevel: String,
        nearbySafeHavens: List<SafeHaven>,
        routeHazards: List<HazardReport>,
        currentHourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): SafetyScoreResult {
        val adaptedReports = routeHazards.map {
            SafetyReportEntity(
                reportId = it.id.toString(),
                type = it.category,
                description = it.description,
                latitude = it.latitude,
                longitude = it.longitude,
                severity = it.severity,
                status = it.status
            )
        }
        return calculateDeterministicSafetyScore(
            lightingLevel = lightingLevel,
            nearbySafeHavens = nearbySafeHavens,
            communityReports = adaptedReports,
            commercialActivityLevel = if (lightingLevel.equals("HIGH", ignoreCase = true)) "HIGH" else "MEDIUM",
            currentHourOfDay = currentHourOfDay
        )
    }
}
