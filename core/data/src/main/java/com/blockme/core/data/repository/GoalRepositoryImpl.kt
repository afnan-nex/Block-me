package com.blockme.core.data.repository

import com.blockme.core.data.local.database.GoalDao
import com.blockme.core.data.local.entity.toDomain
import com.blockme.core.data.local.entity.toEntity
import com.blockme.core.domain.model.FocusGoal
import com.blockme.core.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [GoalRepository] backed by Room.
 * SPDX-License-Identifier: MIT
 */
@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override suspend fun insertOrUpdate(goal: FocusGoal): Long {
        // Check if same text already exists; if so update lastUsed
        val existing = goalDao.getByText(goal.text)
        return if (existing != null) {
            goalDao.insert(existing.copy(lastUsed = System.currentTimeMillis()))
        } else {
            goalDao.insert(goal.toEntity())
        }
    }

    override fun getRecentGoals(limit: Int): Flow<List<FocusGoal>> =
        goalDao.getRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun deleteGoal(id: Long) =
        goalDao.deleteById(id)
}
