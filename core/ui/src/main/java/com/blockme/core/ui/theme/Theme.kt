package com.blockme.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Block me Material3 theme.
 * Supports dynamic color (Android 12+), AMOLED pure-black mode, and dark mode.
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,
    secondary = Teal80,
    onSecondary = Teal20,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,
    background = DarkBg,
    onBackground = LightGray,
    surface = DarkSurface,
    onSurface = LightGray,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = NeutralGray,
    error = ErrorRed,
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = PureBlack,
    surface = PureBlack,
    surfaceVariant = Color(0xFF101010),
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary = Teal40,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,
)

// Make the Color object accessible without ambiguity
private val Color = androidx.compose.ui.graphics.Color

/** LocalAmoledMode composition local for components that need to know. */
val LocalAmoledMode = staticCompositionLocalOf { false }

@Composable
fun BlockMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        amoledMode -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalAmoledMode provides amoledMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BlockMeTypography,
            content = content
        )
    }
}
