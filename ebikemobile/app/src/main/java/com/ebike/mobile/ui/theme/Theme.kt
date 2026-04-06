package com.ebike.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Color scheme
private val md_theme_light_primary = Color(0xFF10B981)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFA3F4D3)
private val md_theme_light_onPrimaryContainer = Color(0xFF002116)
private val md_theme_light_secondary = Color(0xFF516E42)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_tertiary = Color(0xFF396B7D)
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_error = Color(0xFFB3261E)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_background = Color(0xFFFEFDFC)
private val md_theme_light_onBackground = Color(0xFF1C1B1F)
private val md_theme_light_surface = Color(0xFFFEFDFC)
private val md_theme_light_onSurface = Color(0xFF1C1B1F)

private val md_theme_dark_primary = Color(0xFF7DF9C4)
private val md_theme_dark_onPrimary = Color(0xFF003D28)
private val md_theme_dark_primaryContainer = Color(0xFF005A3F)
private val md_theme_dark_onPrimaryContainer = Color(0xFFA3F4D3)
private val md_theme_dark_secondary = Color(0xFFC5E8B8)
private val md_theme_dark_onSecondary = Color(0xFF27401A)
private val md_theme_dark_tertiary = Color(0xFFB0CBDE)
private val md_theme_dark_onTertiary = Color(0xFF1E4D5F)
private val md_theme_dark_error = Color(0xFFF2B8B5)
private val md_theme_dark_onError = Color(0xFF601410)
private val md_theme_dark_background = Color(0xFF1C1B1F)
private val md_theme_dark_onBackground = Color(0xFFE7E0EB)
private val md_theme_dark_surface = Color(0xFF1C1B1F)
private val md_theme_dark_onSurface = Color(0xFFE7E0EB)

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

@Composable
fun EBikeMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)?.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
