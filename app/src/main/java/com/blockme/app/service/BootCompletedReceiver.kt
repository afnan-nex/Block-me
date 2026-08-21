package com.blockme.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.blockme.core.data.local.preferences.UserPreferences
import com.blockme.core.domain.repository.ScheduleRepository
import com.blockme.feature.schedule.ScheduleAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives BOOT_COMPLETED and:
 * 1. Restarts the overlay + timer services if a session is active.
 * 2. Restores all AlarmManager alarms for enabled schedules so repeating schedules persist after reboot.
 *
 * SPDX-License-Identifier: MIT
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var scheduleRepository: ScheduleRepository
    @Inject lateinit var scheduleAlarmScheduler: ScheduleAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val isSessionActive = userPreferences.isSessionActive.first()
                val endTimeMs = userPreferences.sessionEndTime.first()
                val now = System.currentTimeMillis()

                if (isSessionActive && endTimeMs > now) {
                    // Session still active — restart timer service (which also shows overlay)
                    try {
                        ContextCompat.startForegroundService(
                            context,
                            Intent(context, TimerForegroundService::class.java)
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (isSessionActive && endTimeMs <= now) {
                    // Session expired while device was off — mark complete
                    userPreferences.endSession()
                }

                // Restore all enabled schedule alarms on device boot
                val schedules = scheduleRepository.getAllSchedules().first()
                scheduleAlarmScheduler.rescheduleAll(schedules)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}

