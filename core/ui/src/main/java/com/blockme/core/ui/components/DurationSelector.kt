package com.blockme.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockme.core.common.Constants
import com.blockme.core.common.toFormattedTime

/**
 * Duration selector with preset chips and custom hours/minutes picker.
 *
 * Enforces a strict maximum of 3 hours (180 minutes).
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DurationSelector(
    selectedDurationMs: Long,
    onDurationChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustom by remember { mutableStateOf(false) }
    var customHours by remember { mutableStateOf(0) }
    var customMinutes by remember { mutableStateOf(30) }

    val presets = Constants.PRESET_DURATIONS_MINUTES

    Column(modifier = modifier) {
        Text(
            text = "Duration",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Preset chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { minutes ->
                val durationMs = minutes * 60_000L
                val label = if (minutes >= 60) "${minutes / 60}h${if (minutes % 60 != 0) " ${minutes % 60}m" else ""}"
                else "${minutes}m"
                FilterChip(
                    selected = selectedDurationMs == durationMs && !showCustom,
                    onClick = {
                        showCustom = false
                        onDurationChanged(durationMs)
                    },
                    label = { Text(label) }
                )
            }
            FilterChip(
                selected = showCustom,
                onClick = { showCustom = true },
                label = { Text("Custom") }
            )
        }

        // Custom picker
        if (showCustom) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Custom duration",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Hours slider (0-3)
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hours: $customHours",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    Slider(
                        value = customHours.toFloat(),
                        onValueChange = { newHours ->
                            customHours = newHours.toInt()
                            // If 3 hours, force minutes to 0
                            if (customHours >= 3) {
                                customHours = 3
                                customMinutes = 0
                            }
                            onDurationChanged(((customHours * 60 + customMinutes) * 60_000L)
                                .coerceAtMost(Constants.MAX_SESSION_DURATION_MS))
                        },
                        valueRange = 0f..3f,
                        steps = 2,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Minutes slider (0-59), disabled when 3 hours selected
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Minutes: $customMinutes",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    Slider(
                        value = customMinutes.toFloat(),
                        onValueChange = { newMinutes ->
                            if (customHours < 3) {
                                customMinutes = newMinutes.toInt()
                                val totalMs = (customHours * 60 + customMinutes) * 60_000L
                                if (totalMs <= Constants.MAX_SESSION_DURATION_MS && totalMs > 0) {
                                    onDurationChanged(totalMs)
                                }
                            }
                        },
                        valueRange = 0f..59f,
                        steps = 58,
                        enabled = customHours < 3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val totalMs = (customHours * 60 + customMinutes) * 60_000L
            val isValid = totalMs in 1..(Constants.MAX_SESSION_DURATION_MS)

            if (!isValid && totalMs > 0) {
                Text(
                    text = "⚠ Maximum 3 hours allowed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selected: ${totalMs.toFormattedTime()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
