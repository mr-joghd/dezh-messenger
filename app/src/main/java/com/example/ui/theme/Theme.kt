package com.starrynightstudio.dezhmessenger.xwpqrs.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.starrynightstudio.dezhmessenger.xwpqrs.DezhTheme

@Composable
fun MyApplicationTheme(
    currentTheme: DezhTheme = DezhTheme.SIMPLE_DARK,
    content: @Composable () -> Unit
) {
    val themeColors = getThemeColors(currentTheme)
    
    val colorScheme = if (themeColors.isDark) {
        darkColorScheme(
            primary = themeColors.primary,
            background = themeColors.background,
            surface = themeColors.surface,
            onPrimary = themeColors.onPrimary,
            onBackground = themeColors.onBackground,
            onSurface = themeColors.onSurface
        )
    } else {
        lightColorScheme(
            primary = themeColors.primary,
            background = themeColors.background,
            surface = themeColors.surface,
            onPrimary = themeColors.onPrimary,
            onBackground = themeColors.onBackground,
            onSurface = themeColors.onSurface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let { w ->
                w.statusBarColor = themeColors.background.toArgb()
                w.navigationBarColor = themeColors.surface.toArgb()
            }
        }
    }

    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
