package com.blockme.app.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.blockme.app.util.PhoneAppLauncher
import com.blockme.app.util.ScreenLockManager
import com.blockme.core.common.Constants
import com.blockme.core.data.local.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

/**
 * AccessibilityService that enforces the lockdown during an active session.
 *
 * Responsibilities:
 * 1. Intercepts navigation buttons (Home, Back, Recents) and immediately locks the device.
 * 2. High-performance asynchronous package caching: zero blocking Binder IPC on the UI thread.
 * 3. Whitelists:
 *    - Block me application.
 *    - SystemUI, status bar pull-down, volume sliders, notification panel, power menu.
 *    - Keyboards / Input Method Editors (Gboard, Samsung Keyboard, SwiftKey, etc.).
 *    - Default Phone dialer during emergency calls.
 *    - Home launchers (which sit harmlessly behind the fullscreen overlay).
 * 4. Blocks prohibited user applications (Settings tamper, WhatsApp, Chrome, YouTube, Games) or Recents overview.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@AndroidEntryPoint
class LockdownAccessibilityService : AccessibilityService() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var screenLockManager: ScreenLockManager
    @Inject lateinit var phoneAppLauncher: PhoneAppLauncher

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // ── Session state ─────────────────────────────────────────────────────────
    @Volatile private var isSessionActive: Boolean = false
    @Volatile private var isPhoneAppOpen: Boolean = false

    // Anti-loop lock debounce timestamp (ms)
    @Volatile private var lastLockTimestamp: Long = 0L
    @Volatile private var lastUnlockTimestamp: Long = 0L

    // In-memory cached package sets to prevent main-thread Binder IPC overhead
    private val cachedLaunchers = Collections.synchronizedSet(mutableSetOf<String>())
    private val cachedInputMethods = Collections.synchronizedSet(mutableSetOf<String>())
    @Volatile private var cachedDialerPackage: String? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                // Record unlock time to prevent immediate re-locking loop during window transition
                lastUnlockTimestamp = SystemClock.uptimeMillis()
                refreshPackageCache()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenReceiver, filter)

        refreshPackageCache()

        scope.launch {
            userPreferences.isSessionActive.collect { active ->
                isSessionActive = active
                if (active) refreshPackageCache()
            }
        }
        scope.launch {
            userPreferences.isPhoneAppOpen.collect { open ->
                isPhoneAppOpen = open
            }
        }
    }

    private fun refreshPackageCache() {
        scope.launch(Dispatchers.IO) {
            try {
                // 1. Query Home Launchers
                val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                val resolveInfos = packageManager.queryIntentActivities(
                    homeIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                val launcherPkgs = resolveInfos.mapNotNull { it.activityInfo?.packageName }
                cachedLaunchers.clear()
                cachedLaunchers.addAll(launcherPkgs)

                // 2. Query Enabled IMEs / Keyboards
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                val imes = imm?.enabledInputMethodList ?: emptyList()
                cachedInputMethods.clear()
                cachedInputMethods.addAll(imes.map { it.packageName })

                // 3. Query Default Dialer
                cachedDialerPackage = phoneAppLauncher.getDefaultDialerPackage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ── Key event interception ────────────────────────────────────────────────

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!isSessionActive) return false

        // POWER key — ALLOWED (screen off/on)
        if (event.keyCode == KeyEvent.KEYCODE_POWER) return false

        // Volume keys — ALLOWED (ring/media volume adjustment without any interference)
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            event.keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
        ) {
            return false
        }

        // Determine if this key is one of the blocked navigation buttons
        val isBlockedKey = when (event.keyCode) {
            KeyEvent.KEYCODE_BACK -> true
            KeyEvent.KEYCODE_HOME -> true
            KeyEvent.KEYCODE_APP_SWITCH -> true
            KeyEvent.KEYCODE_SEARCH -> true
            else -> false
        }

        if (!isBlockedKey) return false

        // Back is allowed if default Phone app is in foreground
        if (event.keyCode == KeyEvent.KEYCODE_BACK && isPhoneAppAllowed()) {
            return false
        }

        // Intercept on ACTION_DOWN to lock immediately
        if (event.action == KeyEvent.ACTION_DOWN) {
            lockAndDismiss()
            return true
        }

        return true
    }

    // ── Window / app change detection ────────────────────────────────────────

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isSessionActive) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
                val packageName = event.packageName?.toString() ?: return
                if (packageName.isBlank()) return

                // 1. Allow our own application
                if (packageName == applicationContext.packageName) {
                    return
                }

                // 2. Allow SystemUI, Volume panels, Notification shade, Quick Settings, Power dialog
                if (isSystemUiOrVolumePackage(packageName)) {
                    val className = event.className?.toString() ?: ""
                    // Only block if user triggered the Recents app switcher overview or SplitScreen
                    val isRecentsOrSplit = className.contains("Recents", ignoreCase = true) ||
                            className.contains("SplitScreen", ignoreCase = true) ||
                            className.contains("OverviewProxy", ignoreCase = true)

                    if (isRecentsOrSplit) {
                        lockAndDismiss()
                    }
                    return
                }

                // 3. Allow default Phone dialer if emergency mode is active
                if (isDialerPackage(packageName)) {
                    if (isPhoneAppAllowed()) {
                        return
                    }
                }

                // 4. Allow Keyboards / Input Method Editors (Gboard, Samsung Honeyboard, SwiftKey, AOSP, etc.)
                if (isInputMethodPackage(packageName)) {
                    return
                }

                // 5. Allow Home Launcher packages (they harmlessly sit under the fullscreen overlay)
                if (isLauncherPackage(packageName)) {
                    if (isPhoneAppOpen) {
                        scope.launch { userPreferences.setPhoneAppOpen(false) }
                    }
                    return
                }

                // 6. If phone app was open and user navigated to ANY other non-phone app -> lock & reset
                if (isPhoneAppOpen) {
                    scope.launch { userPreferences.setPhoneAppOpen(false) }
                }

                // 7. Prohibited External App Launch (Settings tamper, WhatsApp, Chrome, YouTube, Games, etc.)
                // Block immediately!
                lockAndDismiss()
            }
        }
    }

    // ── Fast In-Memory Helpers ────────────────────────────────────────────────

    private fun isPhoneAppAllowed(): Boolean {
        return isPhoneAppOpen && cachedDialerPackage != null
    }

    private fun isDialerPackage(pkg: String): Boolean {
        return pkg == cachedDialerPackage || phoneAppLauncher.isDefaultDialerPackage(pkg)
    }

    private fun isSystemUiOrVolumePackage(pkg: String): Boolean {
        val lower = pkg.lowercase()
        return lower == "android" ||
                lower == "com.android.systemui" ||
                lower == "com.google.android.systemui" ||
                lower.contains("systemui") ||
                lower.contains("volume") ||
                lower.contains("notification") ||
                lower.contains("cocktailbar") ||
                lower.contains("quicksettings") ||
                lower.contains("systemoverlay")
    }

    private fun isInputMethodPackage(pkg: String): Boolean {
        if (cachedInputMethods.contains(pkg)) return true
        val lower = pkg.lowercase()
        return lower.contains("inputmethod") ||
                lower.contains("honeyboard") ||
                lower.contains("swiftkey") ||
                lower.contains("keyboard") ||
                lower.contains("gboard") ||
                lower.contains("fleksy") ||
                lower.contains("facemoji") ||
                lower.contains("latin") ||
                lower.contains("ime")
    }

    private fun isLauncherPackage(pkg: String): Boolean {
        if (cachedLaunchers.contains(pkg)) return true
        val lower = pkg.lowercase()
        return lower.contains("launcher") || lower.contains("home") || lower.contains("nexuslauncher")
    }

    private fun lockAndDismiss() {
        val now = SystemClock.uptimeMillis()
        // Prevent infinite re-locking loop: minimum 3.0s between consecutive lock triggers,
        // and grace period of 2.5s after unlocking device
        if (now - lastLockTimestamp < 3000L || now - lastUnlockTimestamp < 2500L) {
            return
        }
        lastLockTimestamp = now

        // Dismiss the foreground app immediately back to home
        performGlobalAction(GLOBAL_ACTION_HOME)

        // Lock screen immediately via DevicePolicyManager
        screenLockManager.lockNow()

        // Fallback global action lock screen for Android 9+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } catch (e: Exception) {
                // Ignore if not supported
            }
        }

        incrementTemptation()
    }

    private fun incrementTemptation() {
        scope.launch {
            val intent = Intent("com.blockme.app.ACTION_TEMPTATION").apply {
                setPackage(packageName)
            }
            sendBroadcast(intent)
        }
    }

    override fun onInterrupt() {
        // Service interrupted by system
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        scope.cancel()
        super.onDestroy()
    }
}
