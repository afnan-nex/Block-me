package com.blockme.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blockme.core.common.Constants

/**
 * Material 3 Duration Selector.
 *
 * Displays a 2x3 enlarged preset grid (15m, 30m, 45m / 1h, 2h, 3h) and full-width adjustment sliders.
 * Minimum duration: 1 minute (60,000 ms).
 * Maximum duration: 3 hours (180 minutes).
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun DurationSelector(
    selectedDurationMs: Long,
    onDurationChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMinutes = (selectedDurationMs / 60_000L).coerceIn(1, 180).toInt()
    val hours = totalMinutes / 60
    val rawMinutes = totalMinutes % 60

    var rememberedMinutes by remember { mutableIntStateOf(if (rawMinutes > 0) rawMinutes else 30) }
    if (hours < 3 && rawMinutes > 0) {
        rememberedMinutes = rawMinutes
    }

    val currentMinutes = if (hours == 3) rememberedMinutes else rawMinutes

    val row1 = listOf(15 to "15m", 30 to "30m", 45 to "45m")
    val row2 = listOf(60 to "1h", 120 to "2h", 180 to "3h")

    Column(modifier = modifier) {
        // Row 1 of enlarged buttons: 15m, 30m, 45m
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row1.forEach { (presetMin, label) ->
                val durationMs = presetMin * 60_000L
                val isSelected = selectedDurationMs == durationMs
                if (isSelected) {
                    Button(
                        onClick = { onDurationChanged(durationMs) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onDurationChanged(durationMs) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2 of enlarged buttons: 1h, 2h, 3h
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row2.forEach { (presetMin, label) ->
                val durationMs = presetMin * 60_000L
                val isSelected = selectedDurationMs == durationMs
                if (isSelected) {
                    Button(
                        onClick = { onDurationChanged(durationMs) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { onDurationChanged(durationMs) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hours Slider (0-3) spanning full width
        Slider(
            value = hours.toFloat(),
            onValueChange = { newHoursFloat ->
                val newHours = newHoursFloat.toInt()
                if (newHours >= 3) {
                    onDurationChanged(Constants.MAX_SESSION_DURATION_MS)
                } else {
                    var mins = currentMinutes
                    if (newHours == 0 && mins < 1) {
                        mins = 1
                    }
                    val totalMs = ((newHours * 60 + mins) * 60_000L)
                        .coerceIn(60_000L, Constants.MAX_SESSION_DURATION_MS)
                    onDurationChanged(totalMs)
                }
            },
            valueRange = 0f..3f,
            steps = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Minutes Slider spanning full width (disabled / greyed out when hours == 3)
        val minMinuteValue = if (hours == 0) 1f else 0f
        val minuteSteps = if (hours == 0) 57 else 58

        Slider(
            value = currentMinutes.toFloat().coerceIn(minMinuteValue, 59f),
            onValueChange = { newMinutesFloat ->
                if (hours < 3) {
                    var newMinutes = newMinutesFloat.toInt()
                    if (hours == 0 && newMinutes < 1) {
                        newMinutes = 1
                    }
                    rememberedMinutes = newMinutes
                    val totalMs = ((hours * 60 + newMinutes) * 60_000L)
                        .coerceIn(60_000L, Constants.MAX_SESSION_DURATION_MS)
                    onDurationChanged(totalMs)
                }
            },
            valueRange = minMinuteValue..59f,
            steps = minuteSteps,
            enabled = hours < 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
