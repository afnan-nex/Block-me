package com.blockme.core.domain.model

/**
 * Domain model for a saved focus goal.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
data class FocusGoal(
    val id: Long = 0,
    val text: String,
    val lastUsed: Long = System.currentTimeMillis()
)
