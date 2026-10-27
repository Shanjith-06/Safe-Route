package com.example.saferoute.ui.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saferoute.SafeRouteApplication
import com.example.saferoute.ui.hazards.HazardsViewModel
import com.example.saferoute.ui.navigation.SafeNavigationViewModel
import com.example.saferoute.ui.safehavens.SafeHavensViewModel
import com.example.saferoute.ui.sos.EmergencyViewModel

class AppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext as SafeRouteApplication
        val container = app.container

        return when {
            modelClass.isAssignableFrom(SafeNavigationViewModel::class.java) -> {
                SafeNavigationViewModel(
                    routesRepository = container.routesRepository,
                    hazardsRepository = container.hazardsRepository,
                    safeHavensRepository = container.safeHavensRepository
                ) as T
            }
            modelClass.isAssignableFrom(SafeHavensViewModel::class.java) -> {
                SafeHavensViewModel(
                    repository = container.safeHavensRepository
                ) as T
            }
            modelClass.isAssignableFrom(HazardsViewModel::class.java) -> {
                HazardsViewModel(
                    repository = container.hazardsRepository
                ) as T
            }
            modelClass.isAssignableFrom(EmergencyViewModel::class.java) -> {
                EmergencyViewModel(
                    context = context,
                    emergencyRepository = container.emergencyRepository,
                    contactsRepository = container.contactsRepository,
                    preferencesRepository = container.userPreferencesRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
