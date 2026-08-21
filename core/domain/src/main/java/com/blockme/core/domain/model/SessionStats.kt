package com.blockme.core.domain.model

/**
 * Aggregated session statistics.
 * SPDX-License-Identifier: MIT
 */
data class SessionStats(
    val totalSessions: Int = 0,
    val totalFocusMs: Long = 0L,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageDurationMs: Long = 0L,
    val totalTemptations: Int = 0,
    val weeklyData: List<DayFocusData> = emptyList(),   // last 7 days
    val monthlyData: List<DayFocusData> = emptyList()   // last 30 days
)

/**
 * Focus hours for a single day (for charts).
 */
data class DayFocusData(
    val dayEpochMs: Long,
    val totalFocusMs: Long,
    val sessionCount: Int
)
