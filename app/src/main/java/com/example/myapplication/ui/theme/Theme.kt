package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = Color.White,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    background = SurfaceParchment,
    onBackground = SlateDark,
    surface = SurfacePure,
    onSurface = SlateDark,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = SlateMuted,
    outline = BorderDefault,
    outlineVariant = SlateOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = EmeraldDeep,
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = GoldAccent,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF3B2700),
    onSecondaryContainer = GoldContainer,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = SlateSubdued,
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

@Composable
fun NoorWaktuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}