package com.blockme.core.domain.repository

import com.blockme.core.domain.model.FocusGoal
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for focus goals.
 * SPDX-License-Identifier: MIT
 */
interface GoalRepository {
    suspend fun insertOrUpdate(goal: FocusGoal): Long
    fun getRecentGoals(limit: Int = 5): Flow<List<FocusGoal>>
    suspend fun deleteGoal(id: Long)
}
