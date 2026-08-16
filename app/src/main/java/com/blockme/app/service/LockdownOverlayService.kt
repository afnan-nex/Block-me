package com.blockme.app.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.blockme.app.util.NotificationHelper
import com.blockme.app.util.PhoneAppLauncher
import com.blockme.core.common.Constants
import com.blockme.core.data.local.preferences.UserPreferences
import com.blockme.feature.overlay.LockdownOverlayContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that owns the full-screen lockdown overlay window.
 *
 * Key behaviors:
 * - Creates a TYPE_APPLICATION_OVERLAY window that sits BELOW the status bar
 * - Window uses FLAG_KEEP_SCREEN_ON (allows manual power-button screen off)
 * - Re-shows overlay on USER_PRESENT broadcast (device unlock)
 * - Ticks the countdown every second using wall-clock time
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@AndroidEntryPoint
class LockdownOverlayService : Service(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var phoneAppLauncher: PhoneAppLauncher

    // Lifecycle plumbing for ComposeView inside a Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Observable state for the overlay composable
    private val remainingMs = mutableLongStateOf(0L)
    private val goalText = mutableStateOf("")
    private val totalMs = mutableLongStateOf(0L)

    private val screenEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    // User has unlocked — ensure overlay is visible
                    if (overlayView == null) showOverlay()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    // Screen going off: we keep timer running but hide overlay view
                    // to avoid unnecessary rendering. The view will be re-added on USER_PRESENT.
                    // We keep the window manager view attached so the service does not stop.
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenEventReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationHelper.buildSessionNotification(0L, "")
        startForeground(Constants.NOTIFICATION_ID_SESSION, notification)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        scope.launch {
            val endTime = userPreferences.sessionEndTime.first()
            val goal = userPreferences.sessionGoal.first()
            val duration = userPreferences.sessionDurationMs.first()
            totalMs.longValue = duration
            goalText.value = goal

            val remaining = endTime - System.currentTimeMillis()
            remainingMs.longValue = remaining.coerceAtLeast(0L)

            if (remaining > 0) {
                showOverlay()
                startCountdownLoop(endTime)
            } else {
                onSessionComplete()
            }
        }

        return START_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return  // already showing

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            // FLAG_NOT_FOCUSABLE: don't steal focus from system UI
            // FLAG_KEEP_SCREEN_ON: prevent idle timeout (still allows power button)
            // FLAG_LAYOUT_IN_SCREEN: fill usable area
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            // Do NOT use FLAG_LAYOUT_NO_LIMITS — that would draw under status bar
            // Instead use SOFT_INPUT_ADJUST_RESIZE so the view sits below status bar
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@LockdownOverlayService)
            setViewTreeViewModelStoreOwner(this@LockdownOverlayService)
            setViewTreeSavedStateRegistryOwner(this@LockdownOverlayService)

            setContent {
                LockdownOverlayContent(
                    remainingMs = remainingMs.longValue,
                    totalMs = totalMs.longValue,
                    goalText = goalText.value,
                    onPhoneButtonClicked = {
                        scope.launch {
                            userPreferences.setPhoneAppOpen(true)
                        }
                        phoneAppLauncher.launchDefaultPhoneApp()
                    }
                )
            }
        }

        overlayView = view
        windowManager.addView(view, params)
    }

    private fun hideOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // View may already be removed
            }
            overlayView = null
        }
    }

    private fun startCountdownLoop(endTimeMs: Long) {
        scope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val remaining = endTimeMs - now
                if (remaining <= 0) {
                    remainingMs.longValue = 0L
                    onSessionComplete()
                    break
                }
                remainingMs.longValue = remaining
                notificationHelper.updateSessionNotification(remaining, goalText.value)
                delay(1000L)
            }
        }
    }

    private fun onSessionComplete() {
        scope.launch {
            userPreferences.endSession()
        }
        hideOverlay()
        // Broadcast to MainActivity to show celebration screen
        sendBroadcast(Intent(Constants.ACTION_SESSION_COMPLETE).apply {
            setPackage(packageName)
        })
        stopSelf()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        unregisterReceiver(screenEventReceiver)
        hideOverlay()
        scope.cancel()
        viewModelStore.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
