package com.example.saferoute.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.saferoute.data.local.db.entity.TrustedContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedContactDao {
    @Query("SELECT * FROM trusted_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<TrustedContactEntity>>

    @Query("SELECT * FROM trusted_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContactsFlow(): Flow<List<TrustedContactEntity>>

    @Query("SELECT * FROM trusted_contacts WHERE isEnabled = 1 ORDER BY isPrimary DESC")
    suspend fun getActiveContacts(): List<TrustedContactEntity>

    @Query("SELECT * FROM trusted_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): TrustedContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<TrustedContactEntity>)

    @Update
    suspend fun updateContact(contact: TrustedContactEntity)

    @Delete
    suspend fun deleteContact(contact: TrustedContactEntity)

    @Query("DELETE FROM trusted_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("UPDATE trusted_contacts SET isPrimary = 0")
    suspend fun clearPrimaryFlag()
}
