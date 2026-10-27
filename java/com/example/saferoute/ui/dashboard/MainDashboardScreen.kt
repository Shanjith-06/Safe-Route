package com.example.saferoute.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saferoute.data.local.datastore.UserPreferencesRepository
import com.example.saferoute.data.local.db.AppDatabase
import com.example.saferoute.domain.model.TriggerType
import com.example.saferoute.ui.di.AppViewModelFactory
import com.example.saferoute.ui.hazards.HazardReportScreen
import com.example.saferoute.ui.hazards.HazardsViewModel
import com.example.saferoute.ui.navigation.AppScreen
import com.example.saferoute.ui.navigation.SafeNavigationScreen
import com.example.saferoute.ui.navigation.SafeNavigationViewModel
import com.example.saferoute.ui.safehavens.SafeHavensScreen
import com.example.saferoute.ui.safehavens.SafeHavensViewModel
import com.example.saferoute.ui.screens.ActiveNavigationScreen
import com.example.saferoute.ui.screens.EmergencyEvidenceScreen
import com.example.saferoute.ui.screens.HomeScreen
import com.example.saferoute.ui.screens.JourneyHistoryScreen
import com.example.saferoute.ui.screens.LoginGuestScreen
import com.example.saferoute.ui.screens.OfflineStatusScreen
import com.example.saferoute.ui.screens.OnboardingScreen
import com.example.saferoute.ui.screens.PermissionSetupScreen
import com.example.saferoute.ui.screens.PrivacyScreen
import com.example.saferoute.ui.screens.RouteComparisonScreen
import com.example.saferoute.ui.screens.SafetyScoreDetailScreen
import com.example.saferoute.ui.screens.SettingsScreen
import com.example.saferoute.ui.screens.SplashScreen
import com.example.saferoute.ui.screens.TrustedContactsScreen
import com.example.saferoute.ui.sos.EmergencySosScreen
import com.example.saferoute.ui.sos.EmergencyViewModel
import com.example.saferoute.ui.theme.DarkNavyCard
import com.example.saferoute.ui.theme.PrimaryEmergency
import com.example.saferoute.ui.theme.SafeGreen
import com.example.saferoute.ui.theme.SecondaryPurple
import com.example.saferoute.ui.theme.WarningAmber

@Composable
fun MainDashboardScreen() {
    val context = LocalContext.current
    val factory = remember { AppViewModelFactory(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val prefs = remember { UserPreferencesRepository(context) }

    val navViewModel: SafeNavigationViewModel = viewModel(factory = factory)
    val havensViewModel: SafeHavensViewModel = viewModel(factory = factory)
    val hazardsViewModel: HazardsViewModel = viewModel(factory = factory)
    val emergencyViewModel: EmergencyViewModel = viewModel(factory = factory)

    val user by db.userDao().getUserFlow().collectAsState(initial = null)
    val settings by prefs.userSettingsFlow.collectAsState(initial = com.example.saferoute.data.local.datastore.UserSettings())

    val havens by havensViewModel.filteredHavens.collectAsState()
    val hazards by hazardsViewModel.filteredHazards.collectAsState()
    val routes by navViewModel.cachedRoutes.collectAsState()
    val selectedRouteId by navViewModel.selectedRouteId.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

    // Intercept back navigation when on a detail screen
    BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.SPLASH) {
        currentScreen = AppScreen.HOME
    }

    val showBottomBar = currentScreen == AppScreen.HOME ||
            currentScreen == AppScreen.SAFE_NAVIGATION ||
            currentScreen == AppScreen.COMMUNITY_REPORTS ||
            currentScreen == AppScreen.TRUSTED_CONTACTS ||
            currentScreen == AppScreen.SETTINGS

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkNavyCard,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { currentScreen = AppScreen.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SafeGreen,
                            selectedTextColor = SafeGreen,
                            indicatorColor = SafeGreen.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SAFE_NAVIGATION,
                        onClick = { currentScreen = AppScreen.SAFE_NAVIGATION },
                        icon = { Icon(Icons.Default.Navigation, contentDescription = "Navigate") },
                        label = { Text("Navigate") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SecondaryPurple,
                            selectedTextColor = SecondaryPurple,
                            indicatorColor = SecondaryPurple.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.COMMUNITY_REPORTS,
                        onClick = { currentScreen = AppScreen.COMMUNITY_REPORTS },
                        icon = { Icon(Icons.Default.Campaign, contentDescription = "Reports") },
                        label = { Text("Reports") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WarningAmber,
                            selectedTextColor = WarningAmber,
                            indicatorColor = WarningAmber.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.TRUSTED_CONTACTS,
                        onClick = { currentScreen = AppScreen.TRUSTED_CONTACTS },
                        icon = { Icon(Icons.Default.People, contentDescription = "Contacts") },
                        label = { Text("Contacts") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SecondaryPurple,
                            selectedTextColor = SecondaryPurple,
                            indicatorColor = SecondaryPurple.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SETTINGS,
                        onClick = { currentScreen = AppScreen.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            AppScreen.SPLASH -> SplashScreen(
                onSplashFinished = {
                    currentScreen = if (!settings.isOnboardingCompleted) AppScreen.ONBOARDING else AppScreen.HOME
                }
            )

            AppScreen.ONBOARDING -> OnboardingScreen(
                onFinished = { currentScreen = AppScreen.PERMISSIONS }
            )

            AppScreen.PERMISSIONS -> PermissionSetupScreen(
                onContinue = { currentScreen = AppScreen.LOGIN_GUEST }
            )

            AppScreen.LOGIN_GUEST -> LoginGuestScreen(
                onLoginSuccess = { currentScreen = AppScreen.HOME }
            )

            AppScreen.HOME -> HomeScreen(
                user = user,
                isDemoMode = settings.isDemoModeEnabled,
                onStartSafeJourneyClick = { currentScreen = AppScreen.ROUTE_COMPARISON },
                onSosClick = {
                    emergencyViewModel.triggerSos(TriggerType.MANUAL_SOS)
                    currentScreen = AppScreen.SOS
                },
                onTrustedContactsClick = { currentScreen = AppScreen.TRUSTED_CONTACTS },
                onSafeHavensClick = { currentScreen = AppScreen.SAFE_HAVENS },
                onCommunityReportsClick = { currentScreen = AppScreen.COMMUNITY_REPORTS },
                onEmergencyEvidenceClick = { currentScreen = AppScreen.EMERGENCY_EVIDENCE },
                onSafetyScoreDetailsClick = { currentScreen = AppScreen.SAFETY_SCORE_DETAIL }
            )

            AppScreen.SAFE_NAVIGATION -> SafeNavigationScreen(
                viewModel = navViewModel,
                safeHavens = havens,
                hazards = hazards,
                onTriggerSos = {
                    emergencyViewModel.triggerSos(TriggerType.MANUAL_SOS)
                    currentScreen = AppScreen.SOS
                },
                onHavenNavigate = { haven ->
                    navViewModel.destinationInput.value = haven.name
                    navViewModel.calculateRoutes()
                    currentScreen = AppScreen.ROUTE_COMPARISON
                }
            )

            AppScreen.ROUTE_COMPARISON -> RouteComparisonScreen(
                routes = routes,
                selectedRouteId = selectedRouteId,
                originName = navViewModel.originInput.value.ifBlank { "Current Location (Downtown)" },
                destinationName = navViewModel.destinationInput.value.ifBlank { "Grand Central Station" },
                onSelectRoute = { route -> navViewModel.selectRoute(route.id) },
                onStartJourney = { route ->
                    navViewModel.selectRoute(route.id)
                    navViewModel.startNavigation()
                    currentScreen = AppScreen.ACTIVE_NAVIGATION
                },
                onBack = { currentScreen = AppScreen.SAFE_NAVIGATION }
            )

            AppScreen.ACTIVE_NAVIGATION -> {
                val selectedRoute = routes.find { it.id == selectedRouteId } ?: routes.firstOrNull()
                if (selectedRoute != null) {
                    ActiveNavigationScreen(
                        activeRoute = selectedRoute,
                        navigationMonitor = navViewModel.navigationMonitor,
                        safeHavens = havens,
                        hazards = hazards,
                        onEndJourney = {
                            navViewModel.stopNavigation()
                            currentScreen = AppScreen.JOURNEY_HISTORY
                        },
                        onSosTrigger = {
                            emergencyViewModel.triggerSos(TriggerType.MANUAL_SOS)
                            currentScreen = AppScreen.SOS
                        }
                    )
                } else {
                    currentScreen = AppScreen.SAFE_NAVIGATION
                }
            }

            AppScreen.SOS -> EmergencySosScreen(
                viewModel = emergencyViewModel
            )

            AppScreen.TRUSTED_CONTACTS -> TrustedContactsScreen(
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.SAFE_HAVENS -> SafeHavensScreen(
                viewModel = havensViewModel,
                onNavigateToHaven = { haven ->
                    navViewModel.destinationInput.value = haven.name
                    navViewModel.calculateRoutes()
                    currentScreen = AppScreen.ROUTE_COMPARISON
                }
            )

            AppScreen.COMMUNITY_REPORTS -> HazardReportScreen(
                viewModel = hazardsViewModel
            )

            AppScreen.SAFETY_SCORE_DETAIL -> SafetyScoreDetailScreen(
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.EMERGENCY_EVIDENCE -> EmergencyEvidenceScreen(
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.JOURNEY_HISTORY -> JourneyHistoryScreen(
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.SETTINGS -> SettingsScreen(
                onBack = { currentScreen = AppScreen.HOME }
            )

            AppScreen.PRIVACY -> PrivacyScreen(
                onBack = { currentScreen = AppScreen.SETTINGS }
            )

            AppScreen.OFFLINE_STATUS -> OfflineStatusScreen(
                onBack = { currentScreen = AppScreen.SETTINGS }
            )
        }
    }
}
