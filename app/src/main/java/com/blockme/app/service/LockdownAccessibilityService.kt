package com.blockme.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.blockme.app.util.PhoneAppLauncher
import com.blockme.app.util.ScreenLockManager
import com.blockme.core.common.Constants
import com.blockme.core.data.local.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Accessibility service that enforces lockdown during active sessions.
 *
 * ## Blocked Actions (trigger [ScreenLockManager.lockNow])
 * - Back button
 * - Home button
 * - Recent Apps button
 * - Quick Settings panel open
 * - Settings > Apps > Block me (App Info page)
 * - Settings > Accessibility settings
 * - Settings > Device Admin settings
 * - Split-screen attempt
 *
 * ## ALLOWED Actions (do NOT trigger lock)
 * - Power button (KEYCODE_POWER) — intentionally not intercepted
 * - Long-press power (power menu) — not intercepted
 * - Notification shade pull-down — not intercepted
 * - Default Phone app (when opened via overlay Phone button)
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@AndroidEntryPoint
class LockdownAccessibilityService : AccessibilityService() {

    @Inject lateinit var screenLockManager: ScreenLockManager
    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var phoneAppLauncher: PhoneAppLauncher

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // ── Session state (kept in sync via coroutine) ────────────────────────────
    @Volatile private var isSessionActive: Boolean = false
    @Volatile private var isPhoneAppOpen: Boolean = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Subscribe to session state
        scope.launch {
            userPreferences.isSessionActive.collect { active ->
                isSessionActive = active
            }
        }
        scope.launch {
            userPreferences.isPhoneAppOpen.collect { open ->
                isPhoneAppOpen = open
            }
        }
    }

    // ── Key event interception ────────────────────────────────────────────────

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!isSessionActive) return false

        // POWER key — ALLOWED. Never intercept.
        if (event.keyCode == KeyEvent.KEYCODE_POWER) return false

        // Determine if this key is one of the blocked navigation buttons
        val isBlockedKey = when (event.keyCode) {
            KeyEvent.KEYCODE_BACK -> true
            KeyEvent.KEYCODE_HOME -> true
            KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> false
        }

        if (!isBlockedKey) return false

        // Back is allowed if the default Phone app is in foreground
        if (event.keyCode == KeyEvent.KEYCODE_BACK && isPhoneAppAllowed()) {
            return false
        }

        // Only intercept on ACTION_DOWN to avoid double-firing
        if (event.action == KeyEvent.ACTION_DOWN) {
            lockAndIncrement()
            return true
        }

        return false
    }

    // ── Window / app change detection ────────────────────────────────────────

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isSessionActive) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                val className = event.className?.toString() ?: return

                // Allow the default Phone app (only when opened via the overlay button)
                if (isPhoneAppAllowed() && phoneAppLauncher.isDefaultDialerPackage(packageName)) {
                    return
                }

                // ── Settings-based tamper detection ──────────────────────────
                if (packageName == Constants.PACKAGE_SETTINGS) {
                    val rootNode = rootInActiveWindow ?: return

                    // Detect "Block me" app info page
                    val blockMeNodes = rootNode.findAccessibilityNodeInfosByText("Block me")
                    if (blockMeNodes.isNotEmpty()) {
                        lockAndIncrement()
                        return
                    }

                    // Detect Accessibility settings page
                    if (className.contains(Constants.CLASS_ACCESSIBILITY_SETTINGS, ignoreCase = true)) {
                        lockAndIncrement()
                        return
                    }

                    // Detect Device Admin settings page
                    if (className.contains(Constants.CLASS_DEVICE_ADMIN, ignoreCase = true)) {
                        lockAndIncrement()
                        return
                    }
                }

                // ── Quick Settings panel detection (multi-OEM) ────────────────
                if (isQuickSettingsWindow(packageName, className)) {
                    lockAndIncrement()
                    return
                }

                // ── Split-screen / PiP attempt ────────────────────────────────
                if (isSplitScreenAttempt(packageName, className)) {
                    lockAndIncrement()
                    return
                }
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Used for detecting Quick Settings expansion on some OEMs
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun isPhoneAppAllowed(): Boolean {
        // The phone app is only whitelisted if explicitly opened via the overlay button
        return isPhoneAppOpen && phoneAppLauncher.getDefaultDialerPackage() != null
    }

    private fun isQuickSettingsWindow(packageName: String, className: String): Boolean {
        if (packageName !in Constants.QUICK_SETTINGS_PACKAGES) return false
        return Constants.QUICK_SETTINGS_CLASS_FRAGMENTS.any { fragment ->
            className.contains(fragment, ignoreCase = true)
        }
    }

    private fun isSplitScreenAttempt(packageName: String, className: String): Boolean {
        // Detect split-screen / recents UI which is typically in SystemUI
        return packageName in Constants.QUICK_SETTINGS_PACKAGES &&
                (className.contains("Recents", ignoreCase = true) ||
                        className.contains("SplitScreen", ignoreCase = true) ||
                        className.contains("DividerView", ignoreCase = true))
    }

    private fun lockAndIncrement() {
        screenLockManager.lockNow()
        incrementTemptation()
    }

    private fun incrementTemptation() {
        scope.launch {
            // Read current session ID and increment temptation in DB
            // This is handled by the repository via the ViewModel
            // We broadcast so the running session service can update the DB record
            val intent = Intent("com.blockme.app.ACTION_TEMPTATION").apply {
                setPackage(packageName)
            }
            sendBroadcast(intent)
        }
    }

    override fun onInterrupt() {
        // Service interrupted by system; nothing to do
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
