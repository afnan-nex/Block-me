package com.blockme.core.domain.model

/**
 * Domain model for a recurring focus schedule.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
data class FocusSchedule(
    val id: Long = 0,
    val label: String = "",
    val hourOfDay: Int,          // 0-23
    val minuteOfHour: Int,       // 0-59
    val durationMs: Long,        // must be ≤ 3 hours
    val goalText: String = "",
    val repeatType: RepeatType = RepeatType.DAILY,
    val customDays: Set<Int> = emptySet(),  // Calendar.MONDAY etc.
    val enabled: Boolean = true
)

enum class RepeatType {
    DAILY,
    WEEKDAYS,   // Mon-Fri
    WEEKENDS,   // Sat-Sun
    CUSTOM      // user-selected days
}
