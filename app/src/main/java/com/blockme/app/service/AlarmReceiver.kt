package com.blockme.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.blockme.core.common.Constants
import com.blockme.core.data.local.preferences.UserPreferences
import com.blockme.core.domain.repository.ScheduleRepository
import com.blockme.feature.schedule.ScheduleAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles scheduled alarm intents.
 *
 * On receiving a scheduled session alarm, starts the timer foreground service and overlay,
 * and reschedules the next recurrence of the schedule.
 * On receiving a reminder alarm, shows a reminder notification.
 *
 * SPDX-License-Identifier: MIT
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var scheduleRepository: ScheduleRepository
    @Inject lateinit var scheduleAlarmScheduler: ScheduleAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        when (intent.action) {
            Constants.ACTION_SCHEDULED_SESSION -> {
                val durationMs = intent.getLongExtra(Constants.EXTRA_DURATION_MS, 0L)
                val goal = intent.getStringExtra(Constants.EXTRA_GOAL) ?: ""
                val scheduleId = intent.getLongExtra(Constants.EXTRA_SCHEDULE_ID, -1L)

                if (durationMs <= 0L) {
                    pendingResult.finish()
                    return
                }

                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    try {
                        val now = System.currentTimeMillis()
                        val endTimeMs = now + durationMs

                        // Write session state atomically to DataStore
                        userPreferences.startSession(
                            endTimeMs = endTimeMs,
                            startTimeMs = now,
                            durationMs = durationMs,
                            goal = goal,
                            sessionId = -1L
                        )

                        // Start Foreground Service for timer & ongoing notification
                        val serviceIntent = Intent(context, TimerForegroundService::class.java)
                        try {
                            ContextCompat.startForegroundService(context, serviceIntent)
                        } catch (e: Exception) {
                            try {
                                context.startService(serviceIntent)
                            } catch (e2: Exception) {
                                e2.printStackTrace()
                            }
                        }

                        // Reschedule next recurrence of this schedule
                        if (scheduleId != -1L) {
                            val schedule = scheduleRepository.getScheduleById(scheduleId)
                            if (schedule != null && schedule.enabled) {
                                scheduleAlarmScheduler.scheduleAlarm(schedule)
                                scheduleAlarmScheduler.scheduleReminder(schedule)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            Constants.ACTION_SESSION_REMINDER -> {
                val goal = intent.getStringExtra(Constants.EXTRA_GOAL) ?: ""
                val durationMs = intent.getLongExtra(Constants.EXTRA_DURATION_MS, 0L)
                val durationLabel = formatDuration(durationMs)
                try {
                    showReminderNotification(context, goal, durationLabel)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }

            else -> {
                pendingResult.finish()
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

