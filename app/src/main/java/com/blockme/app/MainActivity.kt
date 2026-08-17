package com.blockme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blockme.app.navigation.NavGraph
import com.blockme.app.navigation.Screen
import com.blockme.app.service.LockdownOverlayService
import com.blockme.app.service.TimerForegroundService
import com.blockme.core.data.local.preferences.UserPreferences
import com.blockme.core.ui.theme.BlockMeTheme
import com.blockme.feature.permissions.PermissionGateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Material 3 Single-Activity Entry Point.
 *
 * Implements real-time permission enforcement on every resume and state change,
 * dynamic color support, and M3 NavigationBar.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isAmoled by userPreferences.amoledMode.collectAsStateWithLifecycle(initialValue = false)
            val permissionViewModel: PermissionGateViewModel = hiltViewModel()
            val permissionState by permissionViewModel.state.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current

            // Check permissions and active session recovery instantaneously on every resume
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        permissionViewModel.refreshPermissions()
                        // Ensure overlay and timer services are restored if a session is currently active
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            try {
                                val isActive = userPreferences.isSessionActive.first()
                                val endTime = userPreferences.sessionEndTime.first()
                                val now = System.currentTimeMillis()
                                if (isActive && endTime > now) {
                                    startService(android.content.Intent(this@MainActivity, LockdownOverlayService::class.java))
                                    try {
                                        startForegroundService(android.content.Intent(this@MainActivity, TimerForegroundService::class.java))
                                    } catch (e: Exception) {
                                        startService(android.content.Intent(this@MainActivity, TimerForegroundService::class.java))
                                    }
                                } else if (isActive && endTime <= now) {
                                    userPreferences.setSessionActive(false)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            BlockMeTheme(
                amoledMode = isAmoled,
                dynamicColor = true
            ) {
                val navController = rememberNavController()

                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                // Real-time permission gatekeeper: immediately redirect if any permission is revoked
                LaunchedEffect(permissionState.allGranted) {
                    if (!permissionState.allGranted) {
                        if (currentRoute != Screen.PermissionGate.route && currentRoute != null) {
                            navController.navigate(Screen.PermissionGate.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                val startDestination = if (permissionState.allGranted) {
                    Screen.Timer.route
                } else {
                    Screen.PermissionGate.route
                }

                val showBottomBar = currentRoute in listOf(
                    Screen.Timer.route,
                    Screen.Stats.route,
                    Screen.Schedule.route,
                    Screen.Settings.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomBar) {
                            BlockMeBottomNav(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockMeBottomNav(navController: NavController) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        val items = listOf(
            NavigationItem(Screen.Timer.route, Icons.Filled.Timer, Icons.Outlined.Timer, "Timer"),
            NavigationItem(Screen.Stats.route, Icons.Filled.BarChart, Icons.Outlined.BarChart, "Stats"),
            NavigationItem(Screen.Schedule.route, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "Schedule"),
            NavigationItem(Screen.Settings.route, Icons.Filled.Settings, Icons.Outlined.Settings, "Settings"),
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Timer.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}

private data class NavigationItem(
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
