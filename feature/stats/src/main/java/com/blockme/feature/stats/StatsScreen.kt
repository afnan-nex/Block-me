package com.blockme.feature.stats

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockme.core.common.toHoursString
import com.blockme.core.common.toShortDurationString
import com.blockme.core.domain.model.SessionStats
import com.blockme.core.ui.components.MonthlyHeatmap
import com.blockme.core.ui.components.WeeklyBarChart
import com.blockme.core.ui.theme.DarkSurface
import com.blockme.core.ui.theme.DarkSurfaceVariant
import com.blockme.core.ui.theme.Violet

/**
 * Stats screen — shows total sessions, streaks, temptations, weekly bar chart, monthly heatmap.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D0D1A), Color(0xFF0E0E22))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (stats.totalSessions == 0) {
                NoDataView()
            } else {
                StatsGrid(stats)
                Spacer(modifier = Modifier.height(28.dp))

                // Weekly bar chart
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        WeeklyBarChart(data = stats.weeklyData)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly heatmap
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        MonthlyHeatmap(data = stats.monthlyData)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: SessionStats) {
    val items = listOf(
        "Sessions" to "${stats.totalSessions}",
        "Total Focus" to stats.totalFocusMs.toHoursString(),
        "Current Streak" to "${stats.currentStreak} days",
        "Longest Streak" to "${stats.longestStreak} days",
        "Avg Session" to stats.averageDurationMs.toShortDurationString(),
        "Temptations" to "${stats.totalTemptations}"
    )

    Column {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { (label, value) ->
                    StatCard(
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Violet
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.55f)
                )
            )
        }
    }
}

@Composable
private fun NoDataView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📊", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No sessions yet.\nStart your first focus session!",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.5f)
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
