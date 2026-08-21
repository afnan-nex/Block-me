package com.blockme.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blockme.core.domain.model.DayFocusData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Pure-Compose Material 3 stats charts.
 *
 * Utilizes [MaterialTheme.colorScheme] primary and surfaceContainer tokens without hardcoded hexes.
 * SPDX-License-Identifier: MIT
 */

/**
 * Weekly bar chart showing focus hours for the last 7 days.
 */
@Composable
fun WeeklyBarChart(
    data: List<DayFocusData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxFocusMs = data.maxOf { it.totalFocusMs }.coerceAtLeast(1L)
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "This Week",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { day ->
                val fraction = day.totalFocusMs.toFloat() / maxFocusMs.toFloat()
                val barHeight = (fraction * 100).coerceIn(6f, 100f)
                val hasData = day.totalFocusMs > 0

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Hours label
                    if (hasData) {
                        val hours = TimeUnit.MILLISECONDS.toMinutes(day.totalFocusMs) / 60f
                        Text(
                            text = String.format(Locale.getDefault(), "%.1fh", hours),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // M3 Bar
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(barHeight.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (hasData) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Day label
                    Text(
                        text = dayFormat.format(Date(day.dayEpochMs)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Monthly calendar heatmap showing focus intensity using Material 3 primary container steps.
 */
@Composable
fun MonthlyHeatmap(
    data: List<DayFocusData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxFocusMs = data.maxOf { it.totalFocusMs }.coerceAtLeast(1L)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Last 30 Days",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 5 rows × 6 columns = 30 cells
        val chunked = data.chunked(6)
        chunked.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { day ->
                    val fraction = day.totalFocusMs.toFloat() / maxFocusMs.toFloat()
                    val hasFocus = day.totalFocusMs > 0

                    val cellColor = if (hasFocus) {
                        MaterialTheme.colorScheme.primary.copy(alpha = (0.25f + fraction * 0.75f).coerceIn(0.25f, 1f))
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    }

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(cellColor)
                    )
                }
                // Fill remaining cells if less than 6 in last row
                repeat(6 - week.size) {
                    Box(modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // M3 Legend
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            listOf(0.15f, 0.35f, 0.6f, 0.85f, 1.0f).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
