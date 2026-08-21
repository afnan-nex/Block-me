package com.blockme.app.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
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
 * Service that maintains the persistent fullscreen lockdown overlay and mini floating dialer timer.
 * Runs as a standard window overlay service while TimerForegroundService manages the foreground session notification.
 *
 * SPDX-License-Identifier: MIT
 */
@AndroidEntryPoint
class LockdownOverlayService : Service(),
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var phoneAppLauncher: PhoneAppLauncher

    // Lifecycle plumbing for ComposeView inside a Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // Observable state for the overlay composable
    private val remainingMs = mutableLongStateOf(0L)
    private val goalText = mutableStateOf("")
    private val totalMs = mutableLongStateOf(0L)
    private val isPhoneAppMode = mutableStateOf(false)
    private val isUnlockChallengeEnabled = mutableStateOf(true)

    // Current window position for floating mode
    private var floatingX = 40
    private var floatingY = 240
    private var currentLayoutParams: WindowManager.LayoutParams? = null

    // Broadcast receiver for screen-off / screen-on and session completion events
    private val screenEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    // User unlocked device — ensure overlay is showing if active
                    scope.launch {
                        if (userPreferences.isSessionActive.first()) {
                            isUnlockChallengeEnabled.value = userPreferences.unlockChallengeEnabled.first()
                            userPreferences.setPhoneAppOpen(false)
                            showOverlay()
                        } else {
                            removeOverlay()
                            stopSelf()
                        }
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    // Screen turned off (power button pressed) — session keeps running
                }
                Constants.ACTION_SESSION_COMPLETE,
                "com.blockme.app.ACTION_SESSION_COMPLETE" -> {
                    // Session ended from timer or external signal — dismiss immediately
                    removeOverlay()
                    stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Constants.ACTION_SESSION_COMPLETE)
            addAction("com.blockme.app.ACTION_SESSION_COMPLETE")
        }
        registerReceiver(screenEventReceiver, filter)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_HIDE_OVERLAY -> {
                removeOverlay()
                stopSelf()
            }
            Constants.ACTION_SHOW_OVERLAY, null -> {
                scope.launch {
                    val active = userPreferences.isSessionActive.first()
                    isUnlockChallengeEnabled.value = userPreferences.unlockChallengeEnabled.first()
                    goalText.value = userPreferences.sessionGoal.first()
                    totalMs.longValue = userPreferences.sessionDurationMs.first()
                    if (active) {
                        showOverlay()
                        startSessionObserver()
                    } else {
                        removeOverlay()
                        stopSelf()
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun startSessionObserver() {
        scope.launch {
            userPreferences.isSessionActive.collect { active ->
                if (!active) {
                    removeOverlay()
                    stopSelf()
                }
            }
        }
        scope.launch {
            userPreferences.sessionGoal.collect { goal ->
                goalText.value = goal
            }
        }
        scope.launch {
            userPreferences.sessionDurationMs.collect { duration ->
                totalMs.longValue = duration
            }
        }
        scope.launch {
            userPreferences.isPhoneAppOpen.collect { isOpen ->
                isPhoneAppMode.value = isOpen
                updateOverlayWindowParams(isOpen)
            }
        }
        scope.launch {
            userPreferences.unlockChallengeEnabled.collect { enabled ->
                isUnlockChallengeEnabled.value = enabled
            }
        }
        scope.launch {
            while (true) {
                val active = userPreferences.isSessionActive.first()
                if (!active) {
                    onSessionComplete()
                    break
                }
                val endTime = userPreferences.sessionEndTime.first()
                val now = System.currentTimeMillis()
                val remaining = (endTime - now).coerceAtLeast(0L)
                remainingMs.longValue = remaining

                if (remaining <= 0L) {
                    onSessionComplete()
                    break
                }
                delay(500L)
            }
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val params = getOverlayLayoutParams(isPhoneAppMode.value)
        currentLayoutParams = params

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@LockdownOverlayService)
            setViewTreeViewModelStoreOwner(this@LockdownOverlayService)
            setViewTreeSavedStateRegistryOwner(this@LockdownOverlayService)

            setContent {
                LockdownOverlayContent(
                    remainingMs = remainingMs.longValue,
                    totalMs = totalMs.longValue,
                    goalText = goalText.value,
                    isPhoneAppMode = isPhoneAppMode.value,
                    isUnlockChallengeEnabled = isUnlockChallengeEnabled.value,
                    onPhoneButtonClicked = {
                        scope.launch {
                            userPreferences.setPhoneAppOpen(true)
                            phoneAppLauncher.launchDefaultPhoneApp()
                        }
                    },
                    onReturnFromPhoneApp = {
                        scope.launch {
                            userPreferences.setPhoneAppOpen(false)
                        }
                    },
                    onFloatingDrag = { dx, dy ->
                        val metrics = resources.displayMetrics
                        val statusBarHeightPx = (resources.getDimensionPixelSize(
                            resources.getIdentifier("status_bar_height", "dimen", "android")
                        ).takeIf { it > 0 } ?: (48 * metrics.density).toInt()) + (16 * metrics.density).toInt()

                        val minX = (8 * metrics.density).toInt()
                        val maxX = (metrics.widthPixels - (180 * metrics.density)).toInt().coerceAtLeast(minX)
                        val minY = statusBarHeightPx
                        val maxY = (metrics.heightPixels - (120 * metrics.density)).toInt().coerceAtLeast(minY)

                        floatingX = (floatingX + dx.toInt()).coerceIn(minX, maxX)
                        floatingY = (floatingY + dy.toInt()).coerceIn(minY, maxY)
                        currentLayoutParams?.let { p ->
                            p.x = floatingX
                            p.y = floatingY
                            try {
                                windowManager.updateViewLayout(overlayView, p)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onEmergencyUnlockConfirmed = {
                        onSessionComplete()
                    }
                )
            }
        }

        overlayView = view
        try {
            windowManager.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayWindowParams(isPhoneMode: Boolean) {
        val view = overlayView ?: return
        val params = getOverlayLayoutParams(isPhoneMode)
        currentLayoutParams = params
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getOverlayLayoutParams(isPhoneMode: Boolean): WindowManager.LayoutParams {
        return if (isPhoneMode) {
            val metrics = resources.displayMetrics
            val statusBarHeightPx = (resources.getDimensionPixelSize(
                resources.getIdentifier("status_bar_height", "dimen", "android")
            ).takeIf { it > 0 } ?: (48 * metrics.density).toInt()) + (16 * metrics.density).toInt()
            if (floatingY < statusBarHeightPx) {
                floatingY = statusBarHeightPx
            }

            // When in Phone Mode, shrink window to WRAP_CONTENT and position at floating coordinates
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = floatingX
                y = floatingY
            }
        } else {
            // In Fullscreen Lockdown mode
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
    }

    private fun removeOverlay() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    private fun onSessionComplete() {
        scope.launch {
            userPreferences.endSession()
            userPreferences.setPhoneAppOpen(false)
            notificationHelper.cancelSessionNotification()
            notificationHelper.showCompletionNotification()

            // Broadcast completion
            val intent = Intent(Constants.ACTION_SESSION_COMPLETE).apply {
                setPackage(packageName)
            }
            sendBroadcast(intent)

            removeOverlay()
            stopSelf()
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        try {
            unregisterReceiver(screenEventReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        removeOverlay()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
