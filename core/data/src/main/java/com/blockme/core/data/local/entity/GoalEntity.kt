package com.blockme.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blockme.core.domain.model.FocusGoal

/**
 * Room entity for a saved focus goal.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val lastUsed: Long = System.currentTimeMillis()
)

fun GoalEntity.toDomain() = FocusGoal(id = id, text = text, lastUsed = lastUsed)
fun FocusGoal.toEntity() = GoalEntity(id = id, text = text, lastUsed = lastUsed)
