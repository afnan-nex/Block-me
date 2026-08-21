package com.blockme.core.domain.usecase

import com.blockme.core.domain.model.DayFocusData
import com.blockme.core.domain.model.SessionStats
import com.blockme.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Use case to compute aggregated session statistics.
 * SPDX-License-Identifier: MIT
 */
class GetSessionStatsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<SessionStats> =
        sessionRepository.getCompletedSessions().map { sessions ->
            if (sessions.isEmpty()) return@map SessionStats()

            val totalFocusMs = sessions.sumOf { it.durationMs }
            val avgDurationMs = totalFocusMs / sessions.size
            val totalTemptations = sessions.sumOf { it.temptationCount }

            val now = System.currentTimeMillis()
            val weekAgo = now - TimeUnit.DAYS.toMillis(7)
            val monthAgo = now - TimeUnit.DAYS.toMillis(30)

            val weeklyData = buildDayData(sessions.filter { it.startTime >= weekAgo }, weekAgo, now, 7)
            val monthlyData = buildDayData(sessions.filter { it.startTime >= monthAgo }, monthAgo, now, 30)

            val (currentStreak, longestStreak) = computeStreaks(sessions.map { it.startTime })

            SessionStats(
                totalSessions = sessions.size,
                totalFocusMs = totalFocusMs,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                averageDurationMs = avgDurationMs,
                totalTemptations = totalTemptations,
                weeklyData = weeklyData,
                monthlyData = monthlyData
            )
        }

    private fun buildDayData(
        sessions: List<com.blockme.core.domain.model.FocusSession>,
        fromMs: Long,
        toMs: Long,
        days: Int
    ): List<DayFocusData> {
        val calendar = Calendar.getInstance()
        return (0 until days).map { dayOffset ->
            val dayStart = fromMs + TimeUnit.DAYS.toMillis(dayOffset.toLong())
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
            val daySessions = sessions.filter { it.startTime in dayStart until dayEnd }
            DayFocusData(
                dayEpochMs = dayStart,
                totalFocusMs = daySessions.sumOf { it.durationMs },
                sessionCount = daySessions.size
            )
        }
    }

    private fun computeStreaks(sessionStartTimes: List<Long>): Pair<Int, Int> {
        if (sessionStartTimes.isEmpty()) return Pair(0, 0)

        // Get unique days (at midnight) that had a completed session
        val days = sessionStartTimes.map { toMidnightMs(it) }.toSortedSet().toList()

        var longestStreak = 1
        var currentRun = 1
        for (i in 1 until days.size) {
            val diff = days[i] - days[i - 1]
            if (diff == TimeUnit.DAYS.toMillis(1)) {
                currentRun++
                if (currentRun > longestStreak) longestStreak = currentRun
            } else if (diff > TimeUnit.DAYS.toMillis(1)) {
                currentRun = 1
            }
        }

        // Current streak: count backwards from today
        val todayMidnight = toMidnightMs(System.currentTimeMillis())
        var currentStreak = 0
        for (i in days.indices.reversed()) {
            val expected = todayMidnight - TimeUnit.DAYS.toMillis(currentStreak.toLong())
            if (days[i] == expected) {
                currentStreak++
            } else {
                break
            }
        }

        return Pair(currentStreak, longestStreak)
    }

    private fun toMidnightMs(epochMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
