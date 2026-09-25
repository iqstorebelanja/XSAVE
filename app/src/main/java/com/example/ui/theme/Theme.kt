package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val XDarkColorScheme = darkColorScheme(
    primary = XBlue,
    onPrimary = XBlack,
    primaryContainer = XDarkSurfaceVariant,
    onPrimaryContainer = XTextPrimary,
    secondary = XBlue,
    onSecondary = XBlack,
    secondaryContainer = XDarkSurface,
    onSecondaryContainer = XTextPrimary,
    tertiary = XGold,
    onTertiary = XBlack,
    background = XBlack,
    onBackground = XTextPrimary,
    surface = XDarkSurface,
    onSurface = XTextPrimary,
    surfaceVariant = XDarkSurfaceVariant,
    onSurfaceVariant = XTextSecondary,
    outline = XCardBorder,
    outlineVariant = XCardBorder
)

@Composable
fun XSaveTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = XDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = XBlack.toArgb()
                window.navigationBarColor = XBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
