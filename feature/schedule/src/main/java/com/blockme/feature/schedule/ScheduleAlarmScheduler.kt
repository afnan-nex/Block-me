package com.blockme.feature.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.blockme.core.common.Constants
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.model.RepeatType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages AlarmManager scheduling for focus schedules across all 7 days of the week.
 *
 * SPDX-License-Identifier: MIT
 */
@Singleton
class ScheduleAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleAlarm(schedule: FocusSchedule) {
        if (!schedule.enabled) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTriggerMs = getNextTriggerTime(schedule)

        val intent = Intent().apply {
            component = ComponentName(context.packageName, "com.blockme.app.service.AlarmReceiver")
            action = Constants.ACTION_SCHEDULED_SESSION
            putExtra(Constants.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(Constants.EXTRA_DURATION_MS, schedule.durationMs)
            putExtra(Constants.EXTRA_GOAL, schedule.goalText)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (Constants.REQUEST_CODE_SCHEDULE_BASE + schedule.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerMs,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // SCHEDULE_EXACT_ALARM not granted — fall back to standard alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTriggerMs, pendingIntent)
        }
    }

    fun scheduleReminder(schedule: FocusSchedule) {
        if (!schedule.enabled) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTriggerMs = getNextTriggerTime(schedule) - (5 * 60 * 1000L)
        if (nextTriggerMs <= System.currentTimeMillis()) return

        val intent = Intent().apply {
            component = ComponentName(context.packageName, "com.blockme.app.service.AlarmReceiver")
            action = Constants.ACTION_SESSION_REMINDER
            putExtra(Constants.EXTRA_SCHEDULE_ID, schedule.id)
            putExtra(Constants.EXTRA_DURATION_MS, schedule.durationMs)
            putExtra(Constants.EXTRA_GOAL, schedule.goalText)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (Constants.REQUEST_CODE_REMINDER_BASE + schedule.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMs, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTriggerMs, pendingIntent)
        }
    }

    fun cancelAlarm(schedule: FocusSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val scheduleIntent = Intent().apply {
            component = ComponentName(context.packageName, "com.blockme.app.service.AlarmReceiver")
            action = Constants.ACTION_SCHEDULED_SESSION
        }
        val schedulePendingIntent = PendingIntent.getBroadcast(
            context,
            (Constants.REQUEST_CODE_SCHEDULE_BASE + schedule.id).toInt(),
            scheduleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(schedulePendingIntent)

        val reminderIntent = Intent().apply {
            component = ComponentName(context.packageName, "com.blockme.app.service.AlarmReceiver")
            action = Constants.ACTION_SESSION_REMINDER
        }
        val reminderPendingIntent = PendingIntent.getBroadcast(
            context,
            (Constants.REQUEST_CODE_REMINDER_BASE + schedule.id).toInt(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(reminderPendingIntent)
    }

    fun rescheduleAll(schedules: List<FocusSchedule>) {
        for (schedule in schedules) {
            if (schedule.enabled) {
                scheduleAlarm(schedule)
                scheduleReminder(schedule)
            } else {
                cancelAlarm(schedule)
            }
        }
    }

    /**
     * Calculates the next epoch timestamp (ms) when the schedule should trigger,
     * checking across the selected days of the week starting from now.
     */
    fun getNextTriggerTime(schedule: FocusSchedule): Long {
        val activeDays = if (schedule.customDays.isNotEmpty()) {
            schedule.customDays
        } else {
            when (schedule.repeatType) {
                RepeatType.WEEKDAYS -> setOf(
                    Calendar.MONDAY,
                    Calendar.TUESDAY,
                    Calendar.WEDNESDAY,
                    Calendar.THURSDAY,
                    Calendar.FRIDAY
                )
                RepeatType.WEEKENDS -> setOf(
                    Calendar.SATURDAY,
                    Calendar.SUNDAY
                )
                else -> setOf(
                    Calendar.SUNDAY,
                    Calendar.MONDAY,
                    Calendar.TUESDAY,
                    Calendar.WEDNESDAY,
                    Calendar.THURSDAY,
                    Calendar.FRIDAY,
                    Calendar.SATURDAY
                )
            }
        }

        val now = System.currentTimeMillis()

        // Check today and up to 7 days ahead
        for (dayOffset in 0..7) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, schedule.hourOfDay)
                set(Calendar.MINUTE, schedule.minuteOfHour)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            if (activeDays.contains(dayOfWeek) && cal.timeInMillis > now) {
                return cal.timeInMillis
            }
        }

        // Fallback: tomorrow at scheduled time
        val fallback = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, schedule.hourOfDay)
            set(Calendar.MINUTE, schedule.minuteOfHour)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return fallback.timeInMillis
    }
}
