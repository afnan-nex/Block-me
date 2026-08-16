package com.blockme.feature.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockme.core.common.toFormattedTime
import com.blockme.core.ui.components.DurationSelector
import com.blockme.core.ui.components.FocusGoalInput
import com.blockme.core.ui.theme.DarkBg
import com.blockme.core.ui.theme.DarkSurface
import com.blockme.core.ui.theme.Violet

/**
 * Home screen — timer setup with duration selector, goal input, and Start button.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun TimerSetupScreen(
    viewModel: TimerSetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D0D1A), Color(0xFF0E0E22))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Block me",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "How long will you focus?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.55f)
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Big duration display
            Text(
                text = state.selectedDurationMs.toFormattedTime(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Violet,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Duration selector
            DurationSelector(
                selectedDurationMs = state.selectedDurationMs,
                onDurationChanged = viewModel::onDurationChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Focus goal input
            FocusGoalInput(
                value = state.goalText,
                onValueChange = viewModel::onGoalTextChanged,
                recentGoals = state.recentGoals,
                modifier = Modifier.fillMaxWidth()
            )

            // Error message
            AnimatedVisibility(visible = state.errorMessage != null) {
                state.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Start button
            Button(
                onClick = viewModel::onStartSession,
                enabled = !state.isStarting && state.selectedDurationMs > 0 &&
                        state.selectedDurationMs <= com.blockme.core.common.Constants.MAX_SESSION_DURATION_MS,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Violet,
                    disabledContainerColor = Violet.copy(alpha = 0.3f)
                )
            ) {
                if (state.isStarting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "🔒  Start Focus",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Once started, you cannot stop the session.\nThis is the point.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.35f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
