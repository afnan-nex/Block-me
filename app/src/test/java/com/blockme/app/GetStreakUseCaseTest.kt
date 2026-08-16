package com.blockme.app

import com.blockme.core.domain.model.FocusSession
import com.blockme.core.domain.usecase.GetSessionStatsUseCase
import com.blockme.core.domain.repository.SessionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Unit tests for streak calculation in GetSessionStatsUseCase.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
class GetStreakUseCaseTest {

    private fun makeSession(daysAgo: Long, durationMs: Long = 30 * 60_000L): FocusSession {
        val now = System.currentTimeMillis()
        val startTime = now - TimeUnit.DAYS.toMillis(daysAgo)
        return FocusSession(
            startTime = startTime,
            endTime = startTime + durationMs,
            durationMs = durationMs,
            completed = true
        )
    }

    @Test
    fun `no sessions returns zero streaks`() = runTest {
        val repo = mockk<SessionRepository>()
        every { repo.getCompletedSessions() } returns flowOf(emptyList())
        val useCase = GetSessionStatsUseCase(repo)
        val stats = useCase().first()
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.longestStreak)
    }

    @Test
    fun `single session today returns streak of 1`() = runTest {
        val repo = mockk<SessionRepository>()
        every { repo.getCompletedSessions() } returns flowOf(listOf(makeSession(0)))
        val useCase = GetSessionStatsUseCase(repo)
        val stats = useCase().first()
        assertEquals(1, stats.currentStreak)
    }

    @Test
    fun `3 consecutive days returns streak of 3`() = runTest {
        val repo = mockk<SessionRepository>()
        val sessions = listOf(makeSession(0), makeSession(1), makeSession(2))
        every { repo.getCompletedSessions() } returns flowOf(sessions)
        val useCase = GetSessionStatsUseCase(repo)
        val stats = useCase().first()
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }

    @Test
    fun `gap in streak resets current streak but preserves longest`() = runTest {
        val repo = mockk<SessionRepository>()
        // Sessions: today, yesterday, (gap), 5 days ago, 6 days ago, 7 days ago
        val sessions = listOf(
            makeSession(0), makeSession(1), // current streak = 2
            makeSession(5), makeSession(6), makeSession(7) // older streak = 3
        )
        every { repo.getCompletedSessions() } returns flowOf(sessions)
        val useCase = GetSessionStatsUseCase(repo)
        val stats = useCase().first()
        assertEquals(2, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
    }

    @Test
    fun `total sessions and hours are computed correctly`() = runTest {
        val repo = mockk<SessionRepository>()
        val sessions = listOf(
            makeSession(0, 60 * 60_000L),  // 1 hour
            makeSession(1, 30 * 60_000L),  // 30 min
        )
        every { repo.getCompletedSessions() } returns flowOf(sessions)
        val useCase = GetSessionStatsUseCase(repo)
        val stats = useCase().first()
        assertEquals(2, stats.totalSessions)
        assertEquals(90 * 60_000L, stats.totalFocusMs)
    }
}
