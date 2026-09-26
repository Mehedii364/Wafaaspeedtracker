package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.WafaApplication
import com.example.ui.components.WafaIcons
import com.example.ui.screens.about.AboutScreen
import com.example.ui.screens.ai.AiMonitorScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.fuel.FuelScreen
import com.example.ui.screens.history.TripDetailsScreen
import com.example.ui.screens.history.TripHistoryScreen
import com.example.ui.screens.livemap.LiveMapScreen
import com.example.ui.screens.maintenance.MaintenanceScreen
import com.example.ui.screens.more.MoreMenuSheet
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.permissions.PermissionCenterScreen
import com.example.ui.screens.replay.TripReplayScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.stats.StatisticsScreen
import com.example.ui.screens.vehicles.VehiclesScreen
import com.example.ui.screens.warnings.WarningCenterScreen
import com.example.ui.theme.CyanNeon

object Routes {
    const val DASHBOARD = "dashboard"
    const val LIVE_MAP = "live_map"
    const val HISTORY = "history"
    const val STATS = "stats"
    const val VEHICLES = "vehicles"
    const val TRIP_DETAILS = "trip_details/{tripId}"
    const val TRIP_REPLAY = "trip_replay/{tripId}"
    const val FUEL = "fuel"
    const val MAINTENANCE = "maintenance"
    const val SEARCH = "search"
    const val AI_MONITOR = "ai_monitor"
    const val WARNINGS = "warnings"
    const val PERMISSIONS = "permissions"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val ONBOARDING = "onboarding"

    fun tripDetails(tripId: Long) = "trip_details/$tripId"
    fun tripReplay(tripId: Long) = "trip_replay/$tripId"
}

@Composable
fun WafaNavApp() {
    val context = LocalContext.current
    val app = context.applicationContext as WafaApplication
    val settingsRepo = app.settingsRepository
    val settings by settingsRepo.settings.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showMoreSheet by remember { mutableStateOf(false) }

    val startDestination = if (settings.hasCompletedOnboarding) Routes.DASHBOARD else Routes.ONBOARDING

    val isTopLevelRoute = currentRoute in listOf(
        Routes.DASHBOARD,
        Routes.LIVE_MAP,
        Routes.HISTORY,
        Routes.STATS,
        Routes.VEHICLES
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelRoute) {
                NavigationBar(
                    containerColor = Color(0xFF0F172A),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(WafaIcons.Speedometer, contentDescription = "Dashboard") },
                        label = { Text("Speed", fontSize = 11.sp) },
                        selected = currentRoute == Routes.DASHBOARD,
                        onClick = {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = Color(0xFF1E293B),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        icon = { Icon(WafaIcons.Map, contentDescription = "Live Map") },
                        label = { Text("Map", fontSize = 11.sp) },
                        selected = currentRoute == Routes.LIVE_MAP,
                        onClick = {
                            navController.navigate(Routes.LIVE_MAP) {
                                popUpTo(Routes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = Color(0xFF1E293B),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_item_live_map")
                    )

                    NavigationBarItem(
                        icon = { Icon(WafaIcons.History, contentDescription = "History") },
                        label = { Text("History", fontSize = 11.sp) },
                        selected = currentRoute == Routes.HISTORY,
                        onClick = {
                            navController.navigate(Routes.HISTORY) {
                                popUpTo(Routes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = Color(0xFF1E293B),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    NavigationBarItem(
                        icon = { Icon(WafaIcons.Stats, contentDescription = "Analytics") },
                        label = { Text("Analytics", fontSize = 11.sp) },
                        selected = currentRoute == Routes.STATS,
                        onClick = {
                            navController.navigate(Routes.STATS) {
                                popUpTo(Routes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = Color(0xFF1E293B),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_item_analytics")
                    )

                    NavigationBarItem(
                        icon = { Icon(WafaIcons.Vehicle, contentDescription = "Garage") },
                        label = { Text("Garage", fontSize = 11.sp) },
                        selected = currentRoute == Routes.VEHICLES,
                        onClick = {
                            navController.navigate(Routes.VEHICLES) {
                                popUpTo(Routes.DASHBOARD) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            selectedTextColor = CyanNeon,
                            indicatorColor = Color(0xFF1E293B),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_item_garage")
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                        label = { Text("More", fontSize = 11.sp) },
                        selected = false,
                        onClick = { showMoreSheet = true },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanNeon,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        ),
                        modifier = Modifier.testTag("nav_item_more")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateToLiveMap = { navController.navigate(Routes.LIVE_MAP) },
                    onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                    onNavigateToVehicles = { navController.navigate(Routes.VEHICLES) },
                    onNavigateToWarningCenter = { navController.navigate(Routes.WARNINGS) }
                )
            }

            composable(Routes.LIVE_MAP) {
                LiveMapScreen()
            }

            composable(Routes.HISTORY) {
                TripHistoryScreen(
                    onNavigateToDetails = { tripId ->
                        navController.navigate(Routes.tripDetails(tripId))
                    },
                    onNavigateToReplay = { tripId ->
                        navController.navigate(Routes.tripReplay(tripId))
                    }
                )
            }

            composable(
                route = Routes.TRIP_DETAILS,
                arguments = listOf(navArgument("tripId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
                TripDetailsScreen(
                    tripId = tripId,
                    onBack = { navController.popBackStack() },
                    onNavigateToReplay = { tId ->
                        navController.navigate(Routes.tripReplay(tId))
                    }
                )
            }

            composable(
                route = Routes.TRIP_REPLAY,
                arguments = listOf(navArgument("tripId") { type = NavType.LongType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
                TripReplayScreen(
                    tripId = tripId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.STATS) {
                StatisticsScreen()
            }

            composable(Routes.VEHICLES) {
                VehiclesScreen(
                    onNavigateToDetails = { /* Can view vehicle info */ }
                )
            }

            composable(Routes.FUEL) {
                FuelScreen()
            }

            composable(Routes.MAINTENANCE) {
                MaintenanceScreen()
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onNavigateToTrip = { tripId ->
                        navController.navigate(Routes.tripDetails(tripId))
                    }
                )
            }

            composable(Routes.AI_MONITOR) {
                AiMonitorScreen()
            }

            composable(Routes.WARNINGS) {
                WarningCenterScreen()
            }

            composable(Routes.PERMISSIONS) {
                PermissionCenterScreen()
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) }
                )
            }

            composable(Routes.ABOUT) {
                AboutScreen()
            }
        }
    }

    if (showMoreSheet) {
        MoreMenuSheet(
            onDismiss = { showMoreSheet = false },
            onNavigateToFuel = { navController.navigate(Routes.FUEL) },
            onNavigateToMaintenance = { navController.navigate(Routes.MAINTENANCE) },
            onNavigateToAiMonitor = { navController.navigate(Routes.AI_MONITOR) },
            onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
            onNavigateToWarningCenter = { navController.navigate(Routes.WARNINGS) },
            onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) },
            onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            onNavigateToAbout = { navController.navigate(Routes.ABOUT) }
        )
    }
}
