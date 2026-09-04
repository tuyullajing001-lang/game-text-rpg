package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val InfiniteGachaColorScheme = darkColorScheme(
    primary = ArcaneGold,
    onPrimary = DarkBackground,
    primaryContainer = RunicPurpleDark,
    onPrimaryContainer = ArcaneGoldLight,
    secondary = PortalCyan,
    onSecondary = DarkBackground,
    tertiary = BloodCrimson,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = BloodCrimson,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = InfiniteGachaColorScheme,
        typography = Typography,
        content = content
    )
}

