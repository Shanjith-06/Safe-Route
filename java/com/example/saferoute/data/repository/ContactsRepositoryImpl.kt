package com.example.saferoute.data.repository

import com.example.saferoute.data.local.db.dao.TrustedContactDao
import com.example.saferoute.data.local.db.entity.TrustedContactEntity
import com.example.saferoute.domain.model.TrustedContact
import com.example.saferoute.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactsRepositoryImpl(
    private val dao: TrustedContactDao
) : ContactsRepository {

    override fun getAllContacts(): Flow<List<TrustedContact>> {
        return dao.getAllContacts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getActiveContacts(): List<TrustedContact> {
        return dao.getActiveContacts().map { it.toDomainModel() }
    }

    override suspend fun addContact(contact: TrustedContact): Long {
        if (contact.isPrimary) {
            dao.clearPrimaryFlag()
        }
        return dao.insertContact(contact.toEntity())
    }

    override suspend fun updateContact(contact: TrustedContact) {
        if (contact.isPrimary) {
            dao.clearPrimaryFlag()
        }
        dao.updateContact(contact.toEntity())
    }

    override suspend fun deleteContact(id: Long) {
        dao.deleteContactById(id)
    }

    override suspend fun setPrimaryContact(id: Long) {
        dao.clearPrimaryFlag()
        val contact = dao.getContactById(id)
        if (contact != null) {
            dao.updateContact(contact.copy(isPrimary = true))
        }
    }

    private fun TrustedContactEntity.toDomainModel() = TrustedContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        isPrimary = isPrimary,
        isEnabled = isEnabled,
        sendSms = sendSms,
        sendLocation = sendLocation
    )

    private fun TrustedContact.toEntity() = TrustedContactEntity(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        isPrimary = isPrimary,
        isEnabled = isEnabled,
        sendSms = sendSms,
        sendLocation = sendLocation
    )
}
