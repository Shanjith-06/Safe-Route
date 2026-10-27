package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saferoute.data.local.db.entity.SafeHavenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeHavenDao {
    @Query("SELECT * FROM safe_havens ORDER BY distanceMeters ASC")
    fun getAllSafeHavens(): Flow<List<SafeHavenEntity>>

    @Query("SELECT * FROM safe_havens ORDER BY distanceMeters ASC")
    fun getAllSafeHavensFlow(): Flow<List<SafeHavenEntity>>

    @Query("SELECT * FROM safe_havens WHERE isOpen24_7 = 1 ORDER BY distanceMeters ASC")
    fun getOpen24_7SafeHavens(): Flow<List<SafeHavenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeHaven(safeHaven: SafeHavenEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSafeHavens(safeHavens: List<SafeHavenEntity>)

    @Query("DELETE FROM safe_havens")
    suspend fun deleteAllSafeHavens()
}
