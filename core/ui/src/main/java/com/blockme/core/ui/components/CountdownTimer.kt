package com.blockme.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blockme.core.common.toFormattedTime
import com.blockme.core.ui.theme.RingEnd
import com.blockme.core.ui.theme.RingStart
import com.blockme.core.ui.theme.DarkSurfaceVariant

/**
 * Animated countdown timer with gradient progress ring.
 *
 * @param remainingMs Remaining time in milliseconds
 * @param totalMs Total session duration in milliseconds
 * @param size Diameter of the ring + timer widget
 * @param strokeWidth Width of the progress ring stroke
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@Composable
fun CountdownTimerWidget(
    remainingMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    strokeWidth: Dp = 14.dp,
    textColor: Color = Color.White
) {
    val progress = if (totalMs > 0) (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "timer_ring_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        // Progress ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val strokePx = strokeWidth.toPx()
            val diameter = canvasSize.minDimension - strokePx
            val topLeft = Offset((canvasSize.width - diameter) / 2f, (canvasSize.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Background track
            drawArc(
                color = DarkSurfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Gradient progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(RingStart, RingEnd, RingStart),
                    center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        // Countdown text — HH:MM:SS
        Text(
            text = remainingMs.toFormattedTime(),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = textColor
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
