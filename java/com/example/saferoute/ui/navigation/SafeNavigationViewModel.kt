package com.example.saferoute.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saferoute.domain.model.HazardReport
import com.example.saferoute.domain.model.SafeHaven
import com.example.saferoute.domain.model.SafetyRoute
import com.example.saferoute.domain.navigation.NavigationMonitor
import com.example.saferoute.domain.navigation.NavigationState
import com.example.saferoute.domain.navigation.NavigationStatus
import com.example.saferoute.domain.repository.HazardsRepository
import com.example.saferoute.domain.repository.RoutesRepository
import com.example.saferoute.domain.repository.SafeHavensRepository
import com.example.saferoute.domain.safety.SafetyScoreCalculator
import com.example.saferoute.domain.safety.SafetyScoreResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SafeNavigationViewModel(
    val routesRepository: RoutesRepository,
    val hazardsRepository: HazardsRepository,
    val safeHavensRepository: SafeHavensRepository,
    val navigationMonitor: NavigationMonitor = NavigationMonitor()
) : ViewModel() {

    var originInput = MutableStateFlow("Current Location (Downtown)")
    var destinationInput = MutableStateFlow("Grand Central Station")

    val cachedRoutes: StateFlow<List<SafetyRoute>> = routesRepository.getCachedRoutes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedRouteId = MutableStateFlow<Long?>(101L)
    val selectedRouteId = _selectedRouteId.asStateFlow()

    val navigationState: StateFlow<NavigationState> = navigationMonitor.state

    private val activeHazards = hazardsRepository.getActiveHazards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val safeHavens = safeHavensRepository.getAllSafeHavens()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedRouteSafetyScore: StateFlow<SafetyScoreResult?> = combine(
        cachedRoutes,
        _selectedRouteId,
        activeHazards,
        safeHavens
    ) { routes, selectedId, hazards, havens ->
        val route = routes.find { it.id == selectedId } ?: routes.firstOrNull()
        if (route != null) {
            SafetyScoreCalculator.calculateRouteSafetyScore(
                lightingLevel = route.lightingLevel,
                nearbySafeHavens = havens,
                routeHazards = hazards
            )
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        // Initial route calculation if empty
        viewModelScope.launch {
            if (cachedRoutes.value.isEmpty()) {
                calculateRoutes()
            }
        }
    }

    fun calculateRoutes() {
        viewModelScope.launch {
            val origin = originInput.value
            val dest = destinationInput.value
            val calculated = routesRepository.calculateSafetyRoutes(
                originName = origin,
                destinationName = dest,
                originLat = 37.7749,
                originLng = -122.4194,
                destLat = 37.7833,
                destLng = -122.4167
            )
            if (calculated.isNotEmpty()) {
                _selectedRouteId.value = calculated.first().id
            }
        }
    }

    fun selectRoute(routeId: Long) {
        _selectedRouteId.value = routeId
    }

    fun startNavigation() {
        val route = cachedRoutes.value.find { it.id == _selectedRouteId.value }
            ?: cachedRoutes.value.firstOrNull() ?: return
        navigationMonitor.startNavigation(route)
    }

    fun stopNavigation() {
        navigationMonitor.stopNavigation()
    }

    fun simulateDeviation() {
        val currentState = navigationState.value
        if (currentState.status == NavigationStatus.NAVIGATING) {
            // Shift user position by 120m away from route
            navigationMonitor.updateLocation(
                lat = currentState.currentLat + 0.002,
                lng = currentState.currentLng + 0.002,
                speedKmH = 18.0
            )
        }
    }

    fun simulateUnexpectedStop() {
        val currentState = navigationState.value
        if (currentState.status == NavigationStatus.NAVIGATING) {
            // Set speed to 0.0 km/h
            navigationMonitor.updateLocation(
                lat = currentState.currentLat + 0.0003,
                lng = currentState.currentLng + 0.0003,
                speedKmH = 0.0
            )
        }
    }

    fun dismissAlert() {
        navigationMonitor.dismissAlertAndContinue()
    }

    fun recalculateRoute() {
        navigationMonitor.triggerRecalculate()
    }
}
