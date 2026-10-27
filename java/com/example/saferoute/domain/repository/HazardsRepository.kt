package com.example.saferoute.domain.repository

import com.example.saferoute.domain.model.HazardReport
import kotlinx.coroutines.flow.Flow

interface HazardsRepository {
    fun getAllHazards(): Flow<List<HazardReport>>
    fun getActiveHazards(): Flow<List<HazardReport>>
    suspend fun reportHazard(hazard: HazardReport): Long
    suspend fun updateHazard(hazard: HazardReport)
    suspend fun deleteHazard(id: Long)
}
