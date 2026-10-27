package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saferoute.data.local.db.entity.EvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence_records ORDER BY timestamp DESC")
    fun getAllEvidenceFlow(): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence_records WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getEvidenceForSessionFlow(sessionId: String): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence_records WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    suspend fun getEvidenceForSession(sessionId: String): List<EvidenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceEntity)

    @Query("SELECT * FROM evidence_records WHERE isSynced = 0")
    suspend fun getUnsyncedEvidence(): List<EvidenceEntity>

    @Query("UPDATE evidence_records SET isSynced = 1 WHERE evidenceId IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}
