package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StekerColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = TextWhitePrimary,
    primaryContainer = CharcoalSurface,
    onPrimaryContainer = TextWhitePrimary,
    secondary = CrimsonDark,
    onSecondary = TextWhitePrimary,
    background = CharcoalDark,
    onBackground = TextWhitePrimary,
    surface = CharcoalSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextWhiteSecondary,
    outline = CharcoalBorder,
    error = CrimsonLight,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We enforce the premium dark brand theme as requested for "hitam/abu gelap, merah, dan putih"
    MaterialTheme(
        colorScheme = StekerColorScheme,
        typography = Typography,
        content = content
    )
}
