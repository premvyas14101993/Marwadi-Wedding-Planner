package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BentoGold,
    onPrimary = RoyalMaroonDark,
    primaryContainer = RoyalMaroon,
    onPrimaryContainer = Color.White,
    secondary = RoyalGoldLight,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = BentoGold,
    tertiary = SaffronLight,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFFAF9F6),
    surface = DarkSurface,
    onSurface = Color(0xFFFAF9F6),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFDCCBC0),
    outline = DarkBorder,
    outlineVariant = Color(0xFF4A3238),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalMaroon,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7E6E8),
    onPrimaryContainer = RoyalMaroonDark,
    secondary = RoyalGoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SaffronOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEDD5),
    onTertiaryContainer = Color(0xFF7C2D12),
    background = WarmIvory,
    onBackground = BentoSlate,
    surface = SurfaceCream,
    onSurface = BentoSlate,
    surfaceVariant = WarmCream,
    onSurfaceVariant = BentoSlateMuted,
    outline = BentoBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = ErrorCrimson,
    onError = Color.White,
    errorContainer = ErrorCrimsonLight,
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun MarwadiWeddingTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
