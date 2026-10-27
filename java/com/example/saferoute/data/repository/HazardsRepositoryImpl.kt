package com.example.saferoute.data.repository

import com.example.saferoute.data.local.db.dao.HazardReportDao
import com.example.saferoute.data.local.db.entity.HazardReportEntity
import com.example.saferoute.domain.model.HazardReport
import com.example.saferoute.domain.repository.HazardsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HazardsRepositoryImpl(
    private val dao: HazardReportDao
) : HazardsRepository {

    override fun getAllHazards(): Flow<List<HazardReport>> {
        return dao.getAllHazards().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getActiveHazards(): Flow<List<HazardReport>> {
        return dao.getActiveHazards().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun reportHazard(hazard: HazardReport): Long {
        return dao.insertHazard(hazard.toEntity())
    }

    override suspend fun updateHazard(hazard: HazardReport) {
        dao.updateHazard(hazard.toEntity())
    }

    override suspend fun deleteHazard(id: Long) {
        dao.deleteHazard(id)
    }

    private fun HazardReportEntity.toDomainModel() = HazardReport(
        id = id,
        title = title,
        category = category,
        description = description,
        latitude = latitude,
        longitude = longitude,
        severity = severity,
        timestamp = timestamp,
        isSynced = isSynced,
        status = status
    )

    private fun HazardReport.toEntity() = HazardReportEntity(
        id = id,
        title = title,
        category = category,
        description = description,
        latitude = latitude,
        longitude = longitude,
        severity = severity,
        timestamp = timestamp,
        isSynced = isSynced,
        status = status
    )
}
