package com.example.readyup.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Color.Black,
    primaryContainer = Surface2,
    onPrimaryContainer = Cyan,
    secondary = Rose,
    onSecondary = Color.White,
    tertiary = Lime,
    background = BgDark,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    outline = BorderColor,
    error = Rose,
)

@Composable
fun ReadyUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ReadyUpTypography,
        content = content
    )
}
