package com.blockme.feature.permissions

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enum of all mandatory permissions the app requires.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
enum class PermissionItem {
    OVERLAY,
    ACCESSIBILITY,
    DEVICE_ADMIN,
    BATTERY_OPTIMIZATION,
    NOTIFICATIONS,
    EXACT_ALARM
}

data class PermissionStatus(
    val item: PermissionItem,
    val isGranted: Boolean
)

data class PermissionGateState(
    val permissions: List<PermissionStatus> = emptyList(),
    val allGranted: Boolean = false
)

@HiltViewModel
class PermissionGateViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PermissionGateState())
    val state: StateFlow<PermissionGateState> = _state.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        viewModelScope.launch {
            val statuses = listOf(
                PermissionStatus(PermissionItem.OVERLAY, isOverlayGranted()),
                PermissionStatus(PermissionItem.ACCESSIBILITY, isAccessibilityGranted()),
                PermissionStatus(PermissionItem.DEVICE_ADMIN, isDeviceAdminGranted()),
                PermissionStatus(PermissionItem.BATTERY_OPTIMIZATION, isBatteryOptimizationIgnored()),
                PermissionStatus(PermissionItem.NOTIFICATIONS, isNotificationsGranted()),
                PermissionStatus(PermissionItem.EXACT_ALARM, isExactAlarmGranted()),
            )
            _state.value = PermissionGateState(
                permissions = statuses,
                allGranted = statuses.all { it.isGranted }
            )
        }
    }

    // ── Permission checks ─────────────────────────────────────────────────────

    private fun isOverlayGranted(): Boolean =
        Settings.canDrawOverlays(context)

    private fun isAccessibilityGranted(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(context.packageName, ignoreCase = true)
    }

    private fun isDeviceAdminGranted(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = ComponentName(context.packageName, "com.blockme.app.service.LockdownDeviceAdminReceiver")
        return dpm.isAdminActive(component)
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun isNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true  // Not required below Android 13
        }
    }

    private fun isExactAlarmGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            am.canScheduleExactAlarms()
        } else {
            true  // Not required below Android 12
        }
    }

    // ── Intent helpers ────────────────────────────────────────────────────────

    fun getIntentForPermission(item: PermissionItem): Intent = when (item) {
        PermissionItem.OVERLAY -> Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        PermissionItem.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        PermissionItem.DEVICE_ADMIN -> {
            val component = ComponentName(
                context.packageName,
                "com.blockme.app.service.LockdownDeviceAdminReceiver"
            )
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Required to lock the screen when blocked actions are detected during focus sessions."
                )
            }
        }
        PermissionItem.BATTERY_OPTIMIZATION -> Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        )
        PermissionItem.NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        PermissionItem.EXACT_ALARM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}"))
        } else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    }
}
