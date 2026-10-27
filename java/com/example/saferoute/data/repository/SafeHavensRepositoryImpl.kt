package com.example.saferoute.data.repository

import com.example.saferoute.data.local.db.dao.SafeHavenDao
import com.example.saferoute.data.local.db.entity.SafeHavenEntity
import com.example.saferoute.domain.model.SafeHaven
import com.example.saferoute.domain.repository.SafeHavensRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SafeHavensRepositoryImpl(
    private val dao: SafeHavenDao
) : SafeHavensRepository {

    override fun getAllSafeHavens(): Flow<List<SafeHaven>> {
        return dao.getAllSafeHavens().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getOpen24_7SafeHavens(): Flow<List<SafeHaven>> {
        return dao.getOpen24_7SafeHavens().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun addSafeHaven(safeHaven: SafeHaven): Long {
        return dao.insertSafeHaven(safeHaven.toEntity())
    }

    private fun SafeHavenEntity.toDomainModel() = SafeHaven(
        id = id,
        name = name,
        type = type,
        address = address,
        latitude = latitude,
        longitude = longitude,
        isVerified = isVerified,
        isOpen24_7 = isOpen24_7,
        phoneNumber = phoneNumber,
        distanceMeters = distanceMeters
    )

    private fun SafeHaven.toEntity() = SafeHavenEntity(
        id = id,
        name = name,
        type = type,
        address = address,
        latitude = latitude,
        longitude = longitude,
        isVerified = isVerified,
        isOpen24_7 = isOpen24_7,
        phoneNumber = phoneNumber,
        distanceMeters = distanceMeters
    )
}
