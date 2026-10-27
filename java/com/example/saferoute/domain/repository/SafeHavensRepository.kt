package com.example.saferoute.domain.repository

import com.example.saferoute.domain.model.SafeHaven
import kotlinx.coroutines.flow.Flow

interface SafeHavensRepository {
    fun getAllSafeHavens(): Flow<List<SafeHaven>>
    fun getOpen24_7SafeHavens(): Flow<List<SafeHaven>>
    suspend fun addSafeHaven(safeHaven: SafeHaven): Long
}
