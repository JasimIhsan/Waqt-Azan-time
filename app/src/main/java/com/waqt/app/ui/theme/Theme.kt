package com.waqt.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WaqtSage,
    onPrimary = WaqtDarkBg,
    primaryContainer = WaqtCardSurface,
    onPrimaryContainer = WaqtMintCream,
    secondary = WaqtTeal,
    onSecondary = WaqtDarkBg,
    background = WaqtDarkBg,
    onBackground = WaqtMintCream,
    surface = WaqtCardSurface,
    onSurface = WaqtMintCream,
    surfaceVariant = WaqtCardSurfaceVariant,
    onSurfaceVariant = WaqtTextSecondary,
    outline = WaqtCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = WaqtForestGreen,
    onPrimary = WaqtMintCream,
    primaryContainer = WaqtSage,
    onPrimaryContainer = WaqtDarkBg,
    secondary = WaqtTeal,
    onSecondary = WaqtMintCream,
    background = WaqtMintCream,
    onBackground = WaqtDarkBg,
    surface = Color(0xFFD8E7D5),
    onSurface = WaqtDarkBg,
    surfaceVariant = Color(0xFFC7DAC4),
    onSurfaceVariant = WaqtDarkBg,
    outline = WaqtForestGreen
)

@Composable
fun WaqtTheme(
    darkTheme: Boolean = true, // Defaults to our sleek midnight theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun AzanTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = WaqtTheme(darkTheme = darkTheme, content = content)

