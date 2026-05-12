package com.example.petrunning2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorSurface,
    background = ColorBg,
    onBackground = ColorTextPrimary,
    surface = ColorSurface,
    onSurface = ColorTextPrimary,
    surfaceVariant = ColorSurfaceSoft,
    onSurfaceVariant = ColorTextSecondary,
    outline = ColorBorder,
    outlineVariant = ColorBorderSubtle,
)

@Composable
fun PetRunning2Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
