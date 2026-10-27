package com.example.saferoute.di

import android.content.Context
import com.example.saferoute.data.local.datastore.UserPreferencesRepository
import com.example.saferoute.data.local.db.AppDatabase
import com.example.saferoute.data.repository.ContactsRepositoryImpl
import com.example.saferoute.data.repository.EmergencyRepositoryImpl
import com.example.saferoute.data.repository.HazardsRepositoryImpl
import com.example.saferoute.data.repository.RoutesRepositoryImpl
import com.example.saferoute.data.repository.SafeHavensRepositoryImpl
import com.example.saferoute.domain.repository.ContactsRepository
import com.example.saferoute.domain.repository.EmergencyRepository
import com.example.saferoute.domain.repository.HazardsRepository
import com.example.saferoute.domain.repository.RoutesRepository
import com.example.saferoute.domain.repository.SafeHavensRepository

interface AppContainer {
    val database: AppDatabase
    val userPreferencesRepository: UserPreferencesRepository
    val contactsRepository: ContactsRepository
    val hazardsRepository: HazardsRepository
    val safeHavensRepository: SafeHavensRepository
    val emergencyRepository: EmergencyRepository
    val routesRepository: RoutesRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val contactsRepository: ContactsRepository by lazy {
        ContactsRepositoryImpl(database.trustedContactDao())
    }

    override val hazardsRepository: HazardsRepository by lazy {
        HazardsRepositoryImpl(database.hazardReportDao())
    }

    override val safeHavensRepository: SafeHavensRepository by lazy {
        SafeHavensRepositoryImpl(database.safeHavenDao())
    }

    override val emergencyRepository: EmergencyRepository by lazy {
        EmergencyRepositoryImpl(database.emergencyEventDao())
    }

    override val routesRepository: RoutesRepository by lazy {
        RoutesRepositoryImpl(database.cachedRouteDao())
    }
}
