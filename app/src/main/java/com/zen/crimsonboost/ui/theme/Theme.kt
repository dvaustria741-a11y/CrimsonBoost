package com.zen.crimsonboost.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CrimsonBoostColorScheme = darkColorScheme(
    primary = Crimson,
    onPrimary = TextPrimary,
    secondary = CrimsonBright,
    background = BackgroundBlack,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary
)

@Composable
fun CrimsonBoostTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as android.app.Activity).window
        window.statusBarColor = BackgroundBlack.toArgb()
        window.navigationBarColor = BackgroundBlack.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    MaterialTheme(
        colorScheme = CrimsonBoostColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
