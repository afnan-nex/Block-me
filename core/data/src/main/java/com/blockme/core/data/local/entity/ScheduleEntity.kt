package com.blockme.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.model.RepeatType

/**
 * Room entity for a recurring focus schedule.
 * SPDX-License-Identifier: MIT
 */
@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String = "",
    val hourOfDay: Int,
    val minuteOfHour: Int,
    val durationMs: Long,
    val goalText: String = "",
    val repeatType: String = RepeatType.DAILY.name,
    val customDays: String = "",    // comma-separated Calendar day ints
    val enabled: Boolean = true
)

fun ScheduleEntity.toDomain() = FocusSchedule(
    id = id,
    label = label,
    hourOfDay = hourOfDay,
    minuteOfHour = minuteOfHour,
    durationMs = durationMs,
    goalText = goalText,
    repeatType = RepeatType.valueOf(repeatType),
    customDays = if (customDays.isBlank()) emptySet()
    else customDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
    enabled = enabled
)

fun FocusSchedule.toEntity() = ScheduleEntity(
    id = id,
    label = label,
    hourOfDay = hourOfDay,
    minuteOfHour = minuteOfHour,
    durationMs = durationMs,
    goalText = goalText,
    repeatType = repeatType.name,
    customDays = customDays.joinToString(","),
    enabled = enabled
)
