package com.blockme.feature.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockme.core.common.toFormattedTime
import com.blockme.core.ui.components.CountdownTimerWidget
import com.blockme.core.ui.theme.BlockMeTheme

/**
 * 42-Word Emergency Pledge Text.
 */
const val EMERGENCY_PLEDGE_TEXT =
    "I acknowledge that I am interrupting my focus session before the planned duration. I understand that developing deep work habits requires strong discipline and resisting immediate gratification. By typing this pledge today, I consciously choose to end my full lockdown session early."

/**
 * Normalizes a text string into a list of lowercase alphanumeric words without punctuation.
 */
fun normalizeWords(text: String): List<String> {
    return text.lowercase()
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
}

/**
 * Material 3 Fullscreen Lockdown Overlay.
 *
 * Features:
 * - Solid status bar coverage (no wallpaper bleed) with accessible icons.
 * - Emergency Phone mode: Draggable mini floating timer popup leaving Phone app fully usable.
 * - 80-Tap Emergency Unlock: Word-by-word verified 42-word pledge challenge without distraction while typing.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun LockdownOverlayContent(
    remainingMs: Long,
    totalMs: Long,
    goalText: String,
    isPhoneAppMode: Boolean,
    onPhoneButtonClicked: () -> Unit,
    onReturnFromPhoneApp: () -> Unit,
    onFloatingDrag: (dx: Float, dy: Float) -> Unit,
    onEmergencyUnlockConfirmed: () -> Unit
) {
    BlockMeTheme(darkTheme = true) {
        var tapCount by remember { mutableIntStateOf(0) }
        var showUnlockChallenge by remember { mutableStateOf(false) }

        if (isPhoneAppMode) {
            // Draggable Floating Mini Timer Popup in Phone App Mode
            FloatingTimerPopup(
                remainingMs = remainingMs,
                onDrag = onFloatingDrag,
                onReturn = onReturnFromPhoneApp
            )
        } else {
            // Fullscreen Lockdown Mode
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Center countdown timer + 120-tap detector
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                tapCount++
                                if (tapCount >= 120) {
                                    tapCount = 0
                                    showUnlockChallenge = true
                                }
                            }
                        ) {
                            CountdownTimerWidget(
                                remainingMs = remainingMs,
                                totalMs = totalMs,
                                size = 280.dp,
                                strokeWidth = 14.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                progressColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (tapCount in 10..119) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Emergency unlock: $tapCount / 120 taps",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (goalText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = goalText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        PulsingMotivationText()
                    }

                    // Bottom section: Emergency Phone Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        FilledIconButton(
                            onClick = onPhoneButtonClicked,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = "Open default Phone app",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // In-Place 80-Tap 42-Word Typing Challenge Sheet
                AnimatedVisibility(
                    visible = showUnlockChallenge,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                    modifier = Modifier.fillMaxSize()
                ) {
                    InPlaceEmergencyUnlockSheet(
                        onDismiss = { showUnlockChallenge = false },
                        onConfirm = {
                            showUnlockChallenge = false
                            onEmergencyUnlockConfirmed()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Compact draggable floating timer popup displayed while inside Phone app.
 */
@Composable
private fun FloatingTimerPopup(
    remainingMs: Long,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onReturn: () -> Unit
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .padding(6.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clickable(onClick = onReturn)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LOCKDOWN ACTIVE (DRAG)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = remainingMs.toFormattedTime(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.Fullscreen,
                contentDescription = "Return to full lockdown",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * In-Place Emergency Typing Challenge Sheet.
 * Validates word-by-word without distracting the user while actively composing words.
 */
@Composable
private fun InPlaceEmergencyUnlockSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var typedText by remember { mutableStateOf("") }
    val targetWords = remember { normalizeWords(EMERGENCY_PLEDGE_TEXT) }
    val typedWords = remember(typedText) { normalizeWords(typedText) }
    val isTypingWordInProgress = remember(typedText) {
        typedText.isNotEmpty() && !typedText.last().isWhitespace()
    }

    // Determine validated completed words vs in-progress word
    val (completedMatchedCount, mistakeMessage, isFullyMatched) = remember(typedWords, targetWords, isTypingWordInProgress) {
        var matched = 0
        var mistake: String? = null

        val wordsToValidateCount = if (isTypingWordInProgress && typedWords.isNotEmpty()) {
            typedWords.size - 1 // Last word is still being typed, don't flag yet
        } else {
            typedWords.size // All typed words are completed
        }

        // Validate all completed words
        for (i in 0 until wordsToValidateCount) {
            if (i < targetWords.size) {
                if (typedWords[i] == targetWords[i]) {
                    matched++
                } else {
                    mistake = "Word #${i + 1} mismatch: expected \"${targetWords[i]}\", got \"${typedWords[i]}\""
                    break
                }
            } else {
                mistake = "Too many extra words"
                break
            }
        }

        // If no mistake in completed words and there is an in-progress word:
        if (mistake == null && isTypingWordInProgress && typedWords.isNotEmpty()) {
            val inProgressIndex = typedWords.size - 1
            if (inProgressIndex < targetWords.size) {
                val currentTyped = typedWords[inProgressIndex]
                val expectedTarget = targetWords[inProgressIndex]
                // Only count as matched if it exactly matches the target word so far
                if (currentTyped == expectedTarget) {
                    matched++
                }
            }
        }

        val allMatched = mistake == null &&
                typedWords.size == targetWords.size &&
                typedWords == targetWords

        Triple(matched, mistake, allMatched)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Emergency Early Exit",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                    FilledIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Type the full 42-word pledge below to unlock early (punctuation/case ignored):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Target pledge text card
                ElevatedCard(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Text(
                        text = EMERGENCY_PLEDGE_TEXT,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = typedText,
                    onValueChange = { typedText = it },
                    label = { Text("Type pledge here ($completedMatchedCount/${targetWords.size} words matched)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isFullyMatched) MaterialTheme.colorScheme.primary
                        else if (mistakeMessage != null) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (mistakeMessage != null) {
                    Text(
                        text = mistakeMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (isFullyMatched) {
                    Text(
                        text = "Perfect match! You typed all ${targetWords.size} words correctly.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Progress: $completedMatchedCount of ${targetWords.size} words matched correctly",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = isFullyMatched,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Stop Lockdown")
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingMotivationText() {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "m3_pulse_alpha"
    )

    Text(
        text = "Stay focused on your objective.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        textAlign = TextAlign.Center
    )
}
