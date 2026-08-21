package com.blockme.core.data.repository

import com.blockme.core.data.local.database.SessionDao
import com.blockme.core.data.local.entity.toDomain
import com.blockme.core.data.local.entity.toEntity
import com.blockme.core.domain.model.FocusSession
import com.blockme.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [SessionRepository] backed by Room.
 * SPDX-License-Identifier: MIT
 */
@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override suspend fun insertSession(session: FocusSession): Long =
        sessionDao.insert(session.toEntity())

    override suspend fun updateSession(session: FocusSession) =
        sessionDao.update(session.toEntity())

    override suspend fun getSessionById(id: Long): FocusSession? =
        sessionDao.getById(id)?.toDomain()

    override fun getAllSessions(): Flow<List<FocusSession>> =
        sessionDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getCompletedSessions(): Flow<List<FocusSession>> =
        sessionDao.getCompleted().map { list -> list.map { it.toDomain() } }

    override suspend fun getSessionsBetween(startMs: Long, endMs: Long): List<FocusSession> =
        sessionDao.getBetween(startMs, endMs).map { it.toDomain() }

    override suspend fun markSessionComplete(id: Long, actualEndTime: Long) =
        sessionDao.markComplete(id, actualEndTime)

    override suspend fun incrementTemptation(id: Long) =
        sessionDao.incrementTemptation(id)

    override suspend fun clearAll() =
        sessionDao.deleteAll()
}
