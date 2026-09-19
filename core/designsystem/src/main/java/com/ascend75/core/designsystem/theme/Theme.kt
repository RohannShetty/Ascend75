package com.ascend75.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AscendPalette.Primary,
    onPrimary = AscendPalette.OnPrimary,
    primaryContainer = AscendPalette.PrimaryContainer,
    onPrimaryContainer = AscendPalette.OnPrimaryContainer,
    inversePrimary = AscendPalette.InversePrimary,

    secondary = AscendPalette.Secondary,
    onSecondary = AscendPalette.OnSecondary,
    secondaryContainer = AscendPalette.SecondaryContainer,
    onSecondaryContainer = AscendPalette.OnSecondaryContainer,

    tertiary = AscendPalette.Tertiary,
    onTertiary = AscendPalette.OnTertiary,
    tertiaryContainer = AscendPalette.TertiaryContainer,
    onTertiaryContainer = AscendPalette.OnTertiaryContainer,

    background = AscendPalette.Background,
    onBackground = AscendPalette.OnBackground,

    surface = AscendPalette.Surface,
    onSurface = AscendPalette.OnSurface,
    surfaceVariant = AscendPalette.SurfaceContainerHighest,
    onSurfaceVariant = AscendPalette.OnSurfaceVariant,
    surfaceTint = AscendPalette.SurfaceTint,

    surfaceContainerLowest = AscendPalette.SurfaceContainerLowest,
    surfaceContainerLow = AscendPalette.SurfaceContainerLow,
    surfaceContainer = AscendPalette.SurfaceContainer,
    surfaceContainerHigh = AscendPalette.SurfaceContainerHigh,
    surfaceContainerHighest = AscendPalette.SurfaceContainerHighest,

    outline = AscendPalette.Outline,
    outlineVariant = AscendPalette.OutlineVariant,

    error = AscendPalette.Error,
    onError = AscendPalette.OnError,
    errorContainer = AscendPalette.ErrorContainer,
    onErrorContainer = AscendPalette.OnErrorContainer
)

@Composable
fun AscendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Ascend 75 enforces the executive dark theme palette ("Zenith Fitness & Growth") by default
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AscendTypography,
        shapes = AscendShapes,
        content = content
    )
}
