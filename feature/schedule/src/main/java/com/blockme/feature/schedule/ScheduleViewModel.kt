package com.blockme.feature.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockme.core.common.Constants
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.model.RepeatType
import com.blockme.core.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    val schedules: StateFlow<List<FocusSchedule>> = scheduleRepository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    fun showAddScheduleDialog() { _showAddDialog.value = true }
    fun hideAddScheduleDialog() { _showAddDialog.value = false }

    fun addSchedule(schedule: FocusSchedule) {
        viewModelScope.launch {
            val id = scheduleRepository.insertSchedule(schedule)
            scheduleAlarm(schedule.copy(id = id))
            scheduleReminder(schedule.copy(id = id))
            hideAddScheduleDialog()
        }
    }

    fun toggleSchedule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            scheduleRepository.setEnabled(id, enabled)
            val schedule = scheduleRepository.getScheduleById(id) ?: return@launch
            if (enabled) {
                scheduleAlarm(schedule)
                scheduleReminder(schedule)
            } else {
                cancelAlarm(schedule)
            }
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            val schedule = scheduleRepository.getScheduleById(id) ?: return@launch
            cancelAlarm(schedule)
            scheduleRepository.deleteSchedule(id)
        }
    }

    // ── AlarmManager helpers ──────────────────────────────────────────────────

    private fun scheduleAlarm(schedule: FocusSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTriggerMs = getNextTriggerTime(schedule)

        val intent = Intent().apply {
            component = ComponentName(context.packageName, "com.blockme.app.service.AlarmReceiver")
            action = Constants.ACTION_SCHEDULED_SESSION
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
            // SCHEDULE_EXACT_ALARM not granted — fall back to inexact
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTriggerMs, pendingIntent)
        }
    }

    private fun scheduleReminder(schedule: FocusSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTriggerMs = getNextTriggerTime(schedule) - (5 * 60 * 1000L)
        if (nextTriggerMs <= System.currentTimeMillis()) return

        val intent = Intent().apply {
            component = ComponentName(context.packageName, "com.blockme.app.service.AlarmReceiver")
            action = Constants.ACTION_SESSION_REMINDER
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

    private fun cancelAlarm(schedule: FocusSchedule) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent().apply {
            component = ComponentName(context.packageName, "com.blockme.app.service.AlarmReceiver")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (Constants.REQUEST_CODE_SCHEDULE_BASE + schedule.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getNextTriggerTime(schedule: FocusSchedule): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hourOfDay)
            set(Calendar.MINUTE, schedule.minuteOfHour)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
