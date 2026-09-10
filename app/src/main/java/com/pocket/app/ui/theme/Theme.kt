package com.pocket.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pocket.app.data.preferences.AppThemeMode

private val SoftLightColorScheme = lightColorScheme(
    primary = SoftLightPrimary,
    onPrimary = SoftLightOnPrimary,
    primaryContainer = SoftLightPrimaryContainer,
    onPrimaryContainer = SoftLightOnPrimaryContainer,
    secondary = SoftLightSecondary,
    onSecondary = SoftLightOnSecondary,
    background = SoftLightBackground,
    surface = SoftLightSurface,
    onSurface = SoftLightOnSurface,
    outline = SoftLightOutline
)

private val SoftDarkColorScheme = darkColorScheme(
    primary = SoftDarkPrimary,
    onPrimary = SoftDarkOnPrimary,
    primaryContainer = SoftDarkPrimaryContainer,
    onPrimaryContainer = SoftDarkOnPrimaryContainer,
    secondary = SoftDarkSecondary,
    onSecondary = SoftDarkOnSecondary,
    background = SoftDarkBackground,
    surface = SoftDarkSurface,
    onSurface = SoftDarkOnSurface,
    outline = SoftDarkOutline
)

@Composable
fun PocketTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemInDark
        AppThemeMode.SOFT_LIGHT -> false
        AppThemeMode.SOFT_DARK -> true
    }

    val colorScheme = if (isDark) SoftDarkColorScheme else SoftLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PocketTypography,
        content = content
    )
}
