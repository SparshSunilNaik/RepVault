package com.example.workouttracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Background,
    primaryContainer = Card,
    onPrimaryContainer = Primary,
    secondary = Accent,
    onSecondary = Background,
    background = Background,
    onBackground = OnBackground,
    surface = Card,
    onSurface = OnCard,
    surfaceVariant = Card,
    onSurfaceVariant = SecondaryText,
    outline = Divider
)

@Composable
fun RepVaultTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
