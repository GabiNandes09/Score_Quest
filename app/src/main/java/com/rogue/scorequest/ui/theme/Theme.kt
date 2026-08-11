package com.rogue.scorequest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1A1200),
    secondary = Gold,
    onSecondary = Color(0xFF1A1200),
    tertiary = Success,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1A1200),
    secondary = Gold,
    onSecondary = Color(0xFF1A1200),
    tertiary = Success,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = Error,
    onError = Color.White
)

@Composable
fun ScoreQuestTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
