package com.blockme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blockme.app.navigation.NavGraph
import com.blockme.app.navigation.Screen
import com.blockme.core.data.local.preferences.UserPreferences
import com.blockme.core.ui.theme.BlockMeTheme
import com.blockme.core.ui.theme.DarkSurface
import com.blockme.core.ui.theme.Violet
import com.blockme.feature.permissions.PermissionGateViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity entry point.
 *
 * On resume, checks if all mandatory permissions are still granted.
 * If not, redirects to PermissionGateScreen.
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
            val permissionViewModel: PermissionGateViewModel = hiltViewModel()
            val permissionState by permissionViewModel.state.collectAsStateWithLifecycle()

            BlockMeTheme(darkTheme = true) {
                val navController = rememberNavController()

                val startDestination = if (permissionState.allGranted) {
                    Screen.Timer.route
                } else {
                    Screen.PermissionGate.route
                }

                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                val showBottomBar = currentRoute in listOf(
                    Screen.Timer.route,
                    Screen.Stats.route,
                    Screen.Schedule.route,
                    Screen.Settings.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF0D0D1A),
                    bottomBar = {
                        if (showBottomBar) {
                            BlockMeBottomNav(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Permission check handled via ViewModel in the Composable
    }
}

@Composable
private fun BlockMeBottomNav(navController: NavController) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    NavigationBar(
        containerColor = DarkSurface,
        contentColor = Color.White
    ) {
        val items = listOf(
            Triple(Screen.Timer.route, Icons.Filled.Timer, "Timer"),
            Triple(Screen.Stats.route, Icons.Filled.BarChart, "Stats"),
            Triple(Screen.Schedule.route, Icons.Filled.CalendarMonth, "Schedule"),
            Triple(Screen.Settings.route, Icons.Filled.Settings, "Settings"),
        )

        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(Screen.Timer.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Violet,
                    selectedTextColor = Violet,
                    indicatorColor = Violet.copy(alpha = 0.15f),
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}
