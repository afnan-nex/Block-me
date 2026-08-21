package com.blockme.app

import com.blockme.core.common.Constants
import com.blockme.core.domain.model.FocusGoal
import com.blockme.core.domain.model.FocusSession
import com.blockme.core.domain.repository.GoalRepository
import com.blockme.core.domain.repository.SessionRepository
import com.blockme.core.domain.usecase.StartSessionUseCase
import com.blockme.core.common.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for StartSessionUseCase validation.
 * SPDX-License-Identifier: MIT
 */
class StartSessionUseCaseTest {

    private lateinit var useCase: StartSessionUseCase
    private val sessionRepository: SessionRepository = mockk()
    private val goalRepository: GoalRepository = mockk()

    @Before
    fun setup() {
        useCase = StartSessionUseCase(sessionRepository, goalRepository)
        coEvery { sessionRepository.insertSession(any()) } returns 1L
        coEvery { goalRepository.insertOrUpdate(any()) } returns 1L
    }

    @Test
    fun `zero duration returns error`() = runTest {
        val result = useCase(durationMs = 0L)
        assertTrue(result is Result.Error)
    }

    @Test
    fun `negative duration returns error`() = runTest {
        val result = useCase(durationMs = -1L)
        assertTrue(result is Result.Error)
    }

    @Test
    fun `duration over 3 hours returns error`() = runTest {
        val overLimit = Constants.MAX_SESSION_DURATION_MS + 1L
        val result = useCase(durationMs = overLimit)
        assertTrue(result is Result.Error)
    }

    @Test
    fun `exactly 3 hours is valid`() = runTest {
        val threeHours = Constants.MAX_SESSION_DURATION_MS
        val result = useCase(durationMs = threeHours)
        assertTrue(result is Result.Success)
    }

    @Test
    fun `30 minutes returns success with correct end time`() = runTest {
        val thirtyMin = 30 * 60 * 1000L
        val before = System.currentTimeMillis()
        val result = useCase(durationMs = thirtyMin)
        val after = System.currentTimeMillis()

        assertTrue(result is Result.Success)
        val session = (result as Result.Success).data
        assertTrue(session.endTime in (before + thirtyMin)..(after + thirtyMin + 100))
    }

    @Test
    fun `session stores goal text when provided`() = runTest {
        val result = useCase(durationMs = 30 * 60 * 1000L, goalText = "Study for exam")
        assertTrue(result is Result.Success)
        assertEquals("Study for exam", (result as Result.Success).data.goalText)
    }

    @Test
    fun `blank goal text is trimmed and allowed`() = runTest {
        val result = useCase(durationMs = 30 * 60 * 1000L, goalText = "   ")
        assertTrue(result is Result.Success)
        assertEquals("", (result as Result.Success).data.goalText)
    }
}
