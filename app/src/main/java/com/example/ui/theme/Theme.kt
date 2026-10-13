package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF70F5FF),
    secondary = ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF321966),
    onSecondaryContainer = Color(0xFFD0BCFF),
    tertiary = AmberFire,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4E2C00),
    onTertiaryContainer = Color(0xFFFFDCC1),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderGlow,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Always maintain dark high-contrast theme for hardware diagnostics tool

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
