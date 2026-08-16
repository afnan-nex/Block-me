package com.blockme.core.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.blockme.core.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for focus sessions.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE completed = 1 ORDER BY startTime DESC")
    fun getCompleted(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE startTime >= :startMs AND startTime < :endMs")
    suspend fun getBetween(startMs: Long, endMs: Long): List<SessionEntity>

    @Query("UPDATE sessions SET completed = 1, actualEndTime = :actualEndTime WHERE id = :id")
    suspend fun markComplete(id: Long, actualEndTime: Long)

    @Query("UPDATE sessions SET temptationCount = temptationCount + 1 WHERE id = :id")
    suspend fun incrementTemptation(id: Long)

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
