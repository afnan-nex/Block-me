package com.blockme.core.common

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import java.util.concurrent.TimeUnit

/**
 * App-wide Kotlin extension functions.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

// DataStore singleton per Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

/**
 * Format milliseconds to HH:MM:SS string.
 */
fun Long.toFormattedTime(): String {
    if (this <= 0L) return "00:00:00"
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(this) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Format milliseconds to a human-readable short string (e.g. "1h 30m").
 */
fun Long.toShortDurationString(): String {
    if (this <= 0L) return "0m"
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

/**
 * Format milliseconds to fractional hours string (e.g. "1.5 hrs").
 */
fun Long.toHoursString(): String {
    val hours = this / (1000.0 * 60 * 60)
    return if (hours < 1.0) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
        "${minutes}m"
    } else {
        String.format("%.1fh", hours)
    }
}

/**
 * Clamp a Long value between min and max.
 */
fun Long.clamp(min: Long, max: Long): Long = maxOf(min, minOf(max, this))

/**
 * Convert minutes to milliseconds.
 */
fun Int.minutesToMs(): Long = this * 60 * 1000L

/**
 * Convert seconds to milliseconds.
 */
fun Int.secondsToMs(): Long = this * 1000L
