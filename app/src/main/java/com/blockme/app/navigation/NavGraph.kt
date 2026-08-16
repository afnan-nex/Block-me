package com.blockme.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.blockme.core.ui.theme.Violet
import com.blockme.feature.permissions.PermissionGateScreen
import com.blockme.feature.schedule.ScheduleScreen
import com.blockme.feature.settings.SettingsScreen
import com.blockme.feature.stats.StatsScreen
import com.blockme.feature.timer.TimerSetupScreen

/**
 * Root navigation graph.
 * SPDX-License-Identifier: GPL-3.0-or-later
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
            .background(Color(0xFF0D0D1A))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(text = "🎉", style = MaterialTheme.typography.displayLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Session Complete!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You stayed focused through the entire session.\nGreat work!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.65f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = Violet)
        ) {
            Text("Start Another Session", color = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
