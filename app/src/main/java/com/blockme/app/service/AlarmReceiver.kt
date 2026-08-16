package com.blockme.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blockme.core.common.Constants
import com.blockme.core.common.dataStore
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that handles scheduled alarm intents.
 *
 * On receiving a scheduled session alarm, starts the overlay and timer services.
 * On receiving a reminder alarm, shows a reminder notification.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.ACTION_SCHEDULED_SESSION -> {
                val durationMs = intent.getLongExtra(Constants.EXTRA_DURATION_MS, 0L)
                val goal = intent.getStringExtra(Constants.EXTRA_GOAL) ?: ""

                if (durationMs <= 0L) return

                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    val now = System.currentTimeMillis()
                    val endTimeMs = now + durationMs

                    // Write session state atomically to DataStore
                    context.dataStore.updateData { prefs ->
                        prefs.toMutablePreferences().apply {
                            set(androidx.datastore.preferences.core.booleanPreferencesKey(Constants.KEY_IS_SESSION_ACTIVE), true)
                            set(longPreferencesKey(Constants.KEY_SESSION_END_TIME), endTimeMs)
                            set(longPreferencesKey(Constants.KEY_SESSION_START_TIME), now)
                            set(longPreferencesKey(Constants.KEY_SESSION_DURATION_MS), durationMs)
                            set(stringPreferencesKey(Constants.KEY_SESSION_GOAL), goal)
                        }
                    }

                    context.startForegroundService(Intent(context, LockdownOverlayService::class.java))
                    context.startForegroundService(Intent(context, TimerForegroundService::class.java))
                }
            }

            Constants.ACTION_SESSION_REMINDER -> {
                val goal = intent.getStringExtra(Constants.EXTRA_GOAL) ?: ""
                val durationMs = intent.getLongExtra(Constants.EXTRA_DURATION_MS, 0L)
                val durationLabel = formatDuration(durationMs)
                // NotificationHelper not injectable in a BroadcastReceiver without WorkManager
                // Use a direct notification for simplicity
                showReminderNotification(context, goal, durationLabel)
            }
        }
    }

    private fun formatDuration(durationMs: Long): String {
        val minutes = durationMs / 60_000L
        return when {
            minutes >= 60 -> "${minutes / 60}h${if (minutes % 60 > 0) " ${minutes % 60}m" else ""}"
            else -> "${minutes}m"
        }
    }

    private fun showReminderNotification(context: Context, goal: String, durationLabel: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val notification = android.app.Notification.Builder(context, Constants.NOTIFICATION_CHANNEL_REMINDER)
            .setContentTitle("Focus session starting soon")
            .setContentText("Your $durationLabel session starts in 5 minutes${if (goal.isNotBlank()) " · $goal" else ""}")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .build()
        manager.notify(Constants.NOTIFICATION_ID_REMINDER, notification)
    }
}
