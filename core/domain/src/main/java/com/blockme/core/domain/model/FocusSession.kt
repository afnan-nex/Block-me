package com.blockme.core.domain.model

/**
 * Domain model for a focus session.
 * SPDX-License-Identifier: MIT
 */
data class FocusSession(
    val id: Long = 0,
    val startTime: Long,        // epoch ms
    val endTime: Long,          // epoch ms (absolute end, wall-clock based)
    val durationMs: Long,       // intended duration
    val goalText: String = "",
    val completed: Boolean = false,
    val temptationCount: Int = 0,
    val actualEndTime: Long? = null  // when user actually finished (may differ from endTime)
)
