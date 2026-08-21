package com.blockme.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blockme.core.data.local.entity.GoalEntity
import com.blockme.core.data.local.entity.ScheduleEntity
import com.blockme.core.data.local.entity.SessionEntity
import com.blockme.core.common.Constants

/**
 * Room database — single source of truth for all locally persisted data.
 * SPDX-License-Identifier: MIT
 */
@Database(
    entities = [SessionEntity::class, GoalEntity::class, ScheduleEntity::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = true
)
abstract class BlockMeDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun goalDao(): GoalDao
    abstract fun scheduleDao(): ScheduleDao
}
