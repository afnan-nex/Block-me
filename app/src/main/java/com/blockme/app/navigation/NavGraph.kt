package com.blockme.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.blockme.feature.permissions.PermissionGateScreen
import com.blockme.feature.schedule.ScheduleScreen
import com.blockme.feature.settings.SettingsScreen
import com.blockme.feature.stats.StatsScreen
import com.blockme.feature.timer.TimerSetupScreen

/**
 * Root navigation graph.
 * SPDX-License-Identifier: MIT
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.PermissionGate.route) {
            PermissionGateScreen(
                onAllGranted = {
                    navController.navigate(Screen.Timer.route) {
                        popUpTo(Screen.PermissionGate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Timer.route) {
            TimerSetupScreen()
        }

        composable(Screen.Stats.route) {
            StatsScreen()
        }

        composable(Screen.Schedule.route) {
            ScheduleScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.Celebration.route) {
            CelebrationScreen(
                onDone = {
                    navController.navigate(Screen.Timer.route) {
                        popUpTo(Screen.Celebration.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun CelebrationScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(text = "🎉", style = MaterialTheme.typography.displayLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You stayed focused through the entire session.\nGreat work!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDone,
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = "Start Another Session",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
