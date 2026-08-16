package com.blockme.feature.overlay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockme.core.ui.components.CountdownTimerWidget
import com.blockme.core.ui.theme.BlockMeTheme
import com.blockme.core.ui.theme.DarkBg
import com.blockme.core.ui.theme.OverlayDark
import com.blockme.core.ui.theme.RingEnd
import com.blockme.core.ui.theme.RingStart
import com.blockme.core.ui.theme.Violet

/**
 * The fullscreen overlay content shown during an active focus session.
 *
 * Layout:
 * - Top padding = status bar inset → overlay does NOT draw over the status bar
 * - Countdown timer ring centered
 * - Goal text below timer
 * - Subtle pulsing "Stay strong" message
 * - Phone button at bottom for emergency calls
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun LockdownOverlayContent(
    remainingMs: Long,
    totalMs: Long,
    goalText: String,
    onPhoneButtonClicked: () -> Unit
) {
    BlockMeTheme(darkTheme = true) {
        // Respect status bar inset — CRITICAL: overlay must NOT cover the status bar
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEA0D0D1A),  // 92% opacity dark top
                            Color(0xF20D0D1A),  // 95% opacity dark bottom
                        )
                    )
                )
                // Top padding from status bar inset — do NOT draw behind status bar
                .padding(top = statusBarPadding.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section: branding
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    Text(
                        text = "FOCUS MODE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 6.sp,
                            color = Violet.copy(alpha = 0.7f)
                        )
                    )
                }

                // Center: timer ring + goal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CountdownTimerWidget(
                        remainingMs = remainingMs,
                        totalMs = totalMs,
                        size = 300.dp,
                        strokeWidth = 16.dp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (goalText.isNotBlank()) {
                        Text(
                            text = goalText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    PulsingMotivationText()
                }

                // Bottom section: Phone button + disclaimer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    Text(
                        text = "Emergency calls only",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FilledIconButton(
                        onClick = onPhoneButtonClicked,
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF1A4731),
                            contentColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Open Phone app for calls",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CALL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF4CAF50).copy(alpha = 0.8f),
                            letterSpacing = 2.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Subtle pulsing motivational text.
 */
@Composable
private fun PulsingMotivationText() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Text(
        text = "You've got this. Stay strong. 💪",
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White.copy(alpha = alpha)
        ),
        textAlign = TextAlign.Center
    )
}
