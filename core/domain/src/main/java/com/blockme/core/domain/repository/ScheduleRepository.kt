package com.blockme.core.domain.repository

import com.blockme.core.domain.model.FocusSchedule
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recurring focus schedules.
 * SPDX-License-Identifier: MIT
 */
interface ScheduleRepository {
    fun getAllSchedules(): Flow<List<FocusSchedule>>
    suspend fun getScheduleById(id: Long): FocusSchedule?
    suspend fun insertSchedule(schedule: FocusSchedule): Long
    suspend fun updateSchedule(schedule: FocusSchedule)
    suspend fun deleteSchedule(id: Long)
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
