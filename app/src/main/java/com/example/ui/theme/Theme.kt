package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DSPPrimary,
    secondary = DSPSecondary,
    tertiary = DSPAccent,
    background = DSPDarkBackground,
    surface = DSPDarkSurface,
    surfaceVariant = DSPDarkSurfaceVariant,
    onPrimary = DSPDarkBackground,
    onSecondary = DSPDarkBackground,
    onBackground = DSPTextPrimary,
    onSurface = DSPTextPrimary,
    onSurfaceVariant = DSPTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = DSPPrimaryDark,
    secondary = DSPSecondary,
    tertiary = DSPAccent,
    background = DSPLightBackground,
    surface = DSPLightSurface,
    surfaceVariant = DSPLightSurfaceVariant,
    onPrimary = DSPLightBackground,
    onSecondary = DSPLightBackground,
    onBackground = DSPLightTextPrimary,
    onSurface = DSPLightTextPrimary,
    onSurfaceVariant = DSPLightTextSecondary
)

@Composable
fun GalaxyDSPTheme(
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
