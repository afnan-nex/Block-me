package com.blockme.feature.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockme.core.common.toShortDurationString
import com.blockme.core.domain.model.FocusSchedule
import com.blockme.core.domain.model.RepeatType
import com.blockme.core.ui.theme.DarkBg
import com.blockme.core.ui.theme.DarkSurface
import com.blockme.core.ui.theme.Violet
import java.util.Locale

/**
 * Schedules screen — list recurring sessions, add/enable/delete.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showAddScheduleDialog,
                containerColor = Violet,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add schedule")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0D0D1A), Color(0xFF0E0E22))))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Schedules",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Auto-start focus sessions on a recurring basis",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (schedules.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗓", style = MaterialTheme.typography.displaySmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No schedules yet.\nTap + to add one!",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White.copy(alpha = 0.5f)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(schedules, key = { it.id }) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                onToggle = { enabled ->
                                    viewModel.toggleSchedule(schedule.id, enabled)
                                },
                                onDelete = { viewModel.deleteSchedule(schedule.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: FocusSchedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.enabled) DarkSurface else DarkSurface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", schedule.hourOfDay, schedule.minuteOfHour),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.enabled) Violet else Color.White.copy(alpha = 0.4f)
                    )
                )
                Text(
                    text = "${schedule.durationMs.toShortDurationString()} · ${schedule.repeatType.label()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.55f)
                    )
                )
                if (schedule.goalText.isNotBlank()) {
                    Text(
                        text = schedule.goalText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    )
                }
            }

            Switch(
                checked = schedule.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = Violet, checkedTrackColor = Violet.copy(alpha = 0.4f))
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete schedule",
                    tint = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun RepeatType.label(): String = when (this) {
    RepeatType.DAILY -> "Every day"
    RepeatType.WEEKDAYS -> "Weekdays"
    RepeatType.WEEKENDS -> "Weekends"
    RepeatType.CUSTOM -> "Custom days"
}
