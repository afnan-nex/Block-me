package com.blockme.app.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import com.blockme.app.util.NotificationHelper
import com.blockme.core.common.Constants
import com.blockme.core.data.local.preferences.UserPreferences
import com.blockme.core.domain.repository.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that runs the timer countdown and updates the persistent notification.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@AndroidEntryPoint
class TimerForegroundService : Service() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var sessionRepository: SessionRepository

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val sessionEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.blockme.app.ACTION_TEMPTATION" -> {
                    scope.launch {
                        val sessionId = userPreferences.currentSessionId.first()
                        if (sessionId >= 0) {
                            sessionRepository.incrementTemptation(sessionId)
                        }
                    }
                }
                "com.blockme.app.ACTION_SESSION_COMPLETE",
                Constants.ACTION_SESSION_COMPLETE -> {
                    notificationHelper.cancelSessionNotification()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction("com.blockme.app.ACTION_TEMPTATION")
            addAction("com.blockme.app.ACTION_SESSION_COMPLETE")
            addAction(Constants.ACTION_SESSION_COMPLETE)
        }
        registerReceiver(sessionEventReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            val endTimeMs = userPreferences.sessionEndTime.first()
            val goalText = userPreferences.sessionGoal.first()
            val remaining = endTimeMs - System.currentTimeMillis()

            if (remaining > 0 && userPreferences.isSessionActive.first()) {
                val notification = notificationHelper.buildSessionNotification(
                    remaining.coerceAtLeast(0L), goalText
                )
                startForeground(Constants.NOTIFICATION_ID_SESSION, notification)
                runTimerLoop(endTimeMs, goalText)
            } else {
                notificationHelper.cancelSessionNotification()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private suspend fun runTimerLoop(endTimeMs: Long, goalText: String) {
        while (scope.isActive) {
            val isActive = userPreferences.isSessionActive.first()
            if (!isActive) {
                // Session was cancelled or completed early
                notificationHelper.cancelSessionNotification()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                break
            }

            val remaining = endTimeMs - System.currentTimeMillis()
            if (remaining <= 0L) {
                handleSessionComplete()
                break
            }
            notificationHelper.updateSessionNotification(remaining, goalText)
            delay(1000L)
        }
    }

    private suspend fun handleSessionComplete() {
        val sessionId = userPreferences.currentSessionId.first()
        if (sessionId >= 0) {
            sessionRepository.markSessionComplete(sessionId, System.currentTimeMillis())
        }
        userPreferences.endSession()
        notificationHelper.cancelSessionNotification()

        // Signal session complete
        sendBroadcast(Intent(Constants.ACTION_SESSION_COMPLETE).apply {
            setPackage(packageName)
        })
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(sessionEventReceiver)
        } catch (e: Exception) {
            // Receiver may not have been registered
        }
        notificationHelper.cancelSessionNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
