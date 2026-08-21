package com.blockme.core.domain.repository

import com.blockme.core.domain.model.FocusSession
import com.blockme.core.domain.model.SessionStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for focus sessions.
 * SPDX-License-Identifier: MIT
 */
interface SessionRepository {
    suspend fun insertSession(session: FocusSession): Long
    suspend fun updateSession(session: FocusSession)
    suspend fun getSessionById(id: Long): FocusSession?
    fun getAllSessions(): Flow<List<FocusSession>>
    fun getCompletedSessions(): Flow<List<FocusSession>>
    suspend fun getSessionsBetween(startMs: Long, endMs: Long): List<FocusSession>
    suspend fun markSessionComplete(id: Long, actualEndTime: Long)
    suspend fun incrementTemptation(id: Long)
    suspend fun clearAll()
}
