package com.blockme.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blockme.core.domain.model.FocusSession

/**
 * Room entity for a focus session.
 * SPDX-License-Identifier: MIT
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val goalText: String = "",
    val completed: Boolean = false,
    val temptationCount: Int = 0,
    val actualEndTime: Long? = null
)

fun SessionEntity.toDomain() = FocusSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
    durationMs = durationMs,
    goalText = goalText,
    completed = completed,
    temptationCount = temptationCount,
    actualEndTime = actualEndTime
)

fun FocusSession.toEntity() = SessionEntity(
    id = id,
    startTime = startTime,
    endTime = endTime,
    durationMs = durationMs,
    goalText = goalText,
    completed = completed,
    temptationCount = temptationCount,
    actualEndTime = actualEndTime
)
