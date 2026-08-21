package com.blockme.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleAlarmScheduler: ScheduleAlarmScheduler
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
            val insertedSchedule = schedule.copy(id = id)
            scheduleAlarmScheduler.scheduleAlarm(insertedSchedule)
            scheduleAlarmScheduler.scheduleReminder(insertedSchedule)
            hideAddScheduleDialog()
        }
    }

    fun toggleSchedule(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            scheduleRepository.setEnabled(id, enabled)
            val schedule = scheduleRepository.getScheduleById(id) ?: return@launch
            if (enabled) {
                scheduleAlarmScheduler.scheduleAlarm(schedule)
                scheduleAlarmScheduler.scheduleReminder(schedule)
            } else {
                scheduleAlarmScheduler.cancelAlarm(schedule)
            }
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            val schedule = scheduleRepository.getScheduleById(id) ?: return@launch
            scheduleAlarmScheduler.cancelAlarm(schedule)
            scheduleRepository.deleteSchedule(id)
        }
    }
}

