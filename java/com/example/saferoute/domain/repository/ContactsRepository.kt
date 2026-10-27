package com.example.saferoute.domain.repository

import com.example.saferoute.domain.model.TrustedContact
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun getAllContacts(): Flow<List<TrustedContact>>
    suspend fun getActiveContacts(): List<TrustedContact>
    suspend fun addContact(contact: TrustedContact): Long
    suspend fun updateContact(contact: TrustedContact)
    suspend fun deleteContact(id: Long)
    suspend fun setPrimaryContact(id: Long)
}
