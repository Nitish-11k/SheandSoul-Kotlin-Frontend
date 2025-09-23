package com.example.sheandsoul_nick.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary = Purple80,
    secondary = Purple40,
    background = White,
    onPrimary = Black,
    onSecondary = White
)

@Composable
fun SheAndSoulNickTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}