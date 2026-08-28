package com.example.ui.theme

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

private val FuncityColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color(0xFF858383),
    secondary = BrandGold,
    onSecondary = OnBrandGold,
    secondaryContainer = BrandGold,
    onSecondaryContainer = OnBrandGold,
    background = WarmBackground,
    onBackground = OnSurfaceText,
    surface = WarmSurface,
    onSurface = OnSurfaceText,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    outline = CardBorder,
    outlineVariant = OutlineColor,
    error = BrandRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = FuncityColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PrimaryDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

