package com.example.saferoute.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidence_records")
data class EvidenceEntity(
    @PrimaryKey
    val evidenceId: String,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // AUDIO, PHOTO
    val filePath: String,
    val durationSeconds: Int = 10,
    val fileSize: Long = 0L,
    val isSynced: Boolean = false
)
