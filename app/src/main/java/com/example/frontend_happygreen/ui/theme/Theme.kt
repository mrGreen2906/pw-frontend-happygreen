package com.example.frontend_happygreen.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Palette di colori più sofisticata per l'app HappyGreen
private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green800,
    secondary = Blue500,
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue800,
    tertiary = Orange500,
    onTertiary = Color.White,
    tertiaryContainer = Orange100,
    onTertiaryContainer = Orange800,
    background = Color.White,
    onBackground = Gray900,
    surface = Color.White,
    onSurface = Gray900,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray700,
    outline = Gray400,
    error = Red500,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red900
)

private val DarkColorScheme = darkColorScheme(
    primary = Green300,
    onPrimary = Green900,
    primaryContainer = Green700,
    onPrimaryContainer = Green100,
    secondary = Blue300,
    onSecondary = Blue900,
    secondaryContainer = Blue700,
    onSecondaryContainer = Blue100,
    tertiary = Orange300,
    onTertiary = Orange900,
    tertiaryContainer = Orange700,
    onTertiaryContainer = Orange100,
    background = Gray900,
    onBackground = Color.White,
    surface = Gray800,
    onSurface = Color.White,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray500,
    error = Red300,
    onError = Red900,
    errorContainer = Red700,
    onErrorContainer = Red100
)

@Composable
fun FrontendhappygreenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}