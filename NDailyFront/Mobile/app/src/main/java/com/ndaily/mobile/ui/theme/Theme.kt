package com.ndaily.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    background = Background,
    onBackground = Foreground,
    surface = Color.White,
    onSurface = Foreground,
    secondary = PrimaryAlt,
    error = Danger,
)

@Composable
fun DailyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = DailyTypography,
        content = content,
    )
}
