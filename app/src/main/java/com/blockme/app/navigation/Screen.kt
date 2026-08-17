package com.blockme.app.navigation

/**
 * Navigation destinations.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
sealed class Screen(val route: String) {
    data object PermissionGate : Screen("permission_gate")
    data object Timer : Screen("timer")
    data object Stats : Screen("stats")
    data object Schedule : Screen("schedule")
    data object Settings : Screen("settings")
    data object Celebration : Screen("celebration")
}
