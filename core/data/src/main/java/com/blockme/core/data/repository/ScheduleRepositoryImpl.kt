package com.blockme.core.data.repository

import com.blockme.core.data.local.database.ScheduleDao
import com.blockme.core.data.local.entity.toDomain
import com.blockme.core.data.local.entity.toEntity
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ScheduleRepository] backed by Room.
 * SPDX-License-Identifier: MIT
 */
@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun getAllSchedules(): Flow<List<FocusSchedule>> =
        scheduleDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getScheduleById(id: Long): FocusSchedule? =
        scheduleDao.getById(id)?.toDomain()

    override suspend fun insertSchedule(schedule: FocusSchedule): Long =
        scheduleDao.insert(schedule.toEntity())

    override suspend fun updateSchedule(schedule: FocusSchedule) =
        scheduleDao.update(schedule.toEntity())

    override suspend fun deleteSchedule(id: Long) =
        scheduleDao.deleteById(id)

    override suspend fun setEnabled(id: Long, enabled: Boolean) =
        scheduleDao.setEnabled(id, enabled)
}
