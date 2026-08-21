package com.blockme.core.domain.usecase

import com.blockme.core.domain.repository.SessionRepository
import javax.inject.Inject

/**
 * Use case to end (complete) an active focus session.
 * SPDX-License-Identifier: MIT
 */
class EndSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(sessionId: Long) {
        val actualEndTime = System.currentTimeMillis()
        sessionRepository.markSessionComplete(sessionId, actualEndTime)
    }
}
