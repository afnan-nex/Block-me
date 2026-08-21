package com.blockme.core.domain.usecase

import com.blockme.core.common.Constants
import com.blockme.core.common.Result
import com.blockme.core.domain.model.FocusSession
import com.blockme.core.domain.repository.GoalRepository
import com.blockme.core.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Use case to validate and start a new focus session.
 *
 * Rules:
 * - Duration must be > 0 and ≤ 3 hours
 * - Calculates absolute end time using wall-clock time
 *
 * SPDX-License-Identifier: MIT
 */
class StartSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val goalRepository: GoalRepository,
) {
    /**
     * @param durationMs Intended duration in milliseconds (must be ≤ [Constants.MAX_SESSION_DURATION_MS])
     * @param goalText Optional focus goal
     * @return Result with the new session, or Error if validation fails
     */
    suspend operator fun invoke(durationMs: Long, goalText: String = ""): Result<FocusSession> {
        if (durationMs <= 0L) {
            return Result.Error("Duration must be greater than zero")
        }
        if (durationMs > Constants.MAX_SESSION_DURATION_MS) {
            return Result.Error("Duration cannot exceed 3 hours")
        }

        val now = System.currentTimeMillis()
        val endTime = now + durationMs

        val session = FocusSession(
            startTime = now,
            endTime = endTime,
            durationMs = durationMs,
            goalText = goalText.trim(),
            completed = false,
            temptationCount = 0
        )

        val id = sessionRepository.insertSession(session)

        // Save goal for quick-select recall
        if (goalText.isNotBlank()) {
            goalRepository.insertOrUpdate(
                com.blockme.core.domain.model.FocusGoal(text = goalText.trim())
            )
        }

        return Result.Success(session.copy(id = id))
    }
}
