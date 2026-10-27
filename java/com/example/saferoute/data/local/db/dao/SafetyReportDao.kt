package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.saferoute.data.local.db.entity.SafetyReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyReportDao {
    @Query("SELECT * FROM safety_reports ORDER BY timestamp DESC")
    fun getAllReportsFlow(): Flow<List<SafetyReportEntity>>

    @Query("SELECT * FROM safety_reports ORDER BY timestamp DESC")
    suspend fun getAllReports(): List<SafetyReportEntity>

    @Query("SELECT * FROM safety_reports WHERE isSynced = 0")
    suspend fun getUnsyncedReports(): List<SafetyReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SafetyReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<SafetyReportEntity>)

    @Update
    suspend fun updateReport(report: SafetyReportEntity)

    @Query("UPDATE safety_reports SET isSynced = 1 WHERE reportId IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
