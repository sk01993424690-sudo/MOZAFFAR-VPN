package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class VpnThemeTokens(
    val isDark: Boolean,
    val backgroundGradient: Brush,
    val cardSurface: Color,
    val cardSurfaceElevated: Color,
    val cardBorder: Color,
    val accentGradient: Brush,
    val connectedColor: Color,
    val reconnectingColor: Color,
    val errorColor: Color,
    val cyanAccent: Color
)

val LocalVpnTokens = staticCompositionLocalOf {
    VpnThemeTokens(
        isDark = true,
        backgroundGradient = Brush.verticalGradient(listOf(DeepObsidianNavy, MidnightNavy, DeepIndigo)),
        cardSurface = ElevatedNavyCard,
        cardSurfaceElevated = ElevatedNavyCardAlt,
        cardBorder = SlateBorderDark,
        accentGradient = Brush.linearGradient(listOf(RoyalViolet, ElectricViolet)),
        connectedColor = EmeraldSecure,
        reconnectingColor = AmberReconnect,
        errorColor = CrimsonAlert,
        cyanAccent = ElectricCyan
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = RoyalViolet,
    onPrimary = Color.White,
    primaryContainer = DeepIndigo,
    onPrimaryContainer = SoftLavender,
    secondary = ElectricCyan,
    onSecondary = DeepObsidianNavy,
    secondaryContainer = Color(0xFF0E3A4D),
    onSecondaryContainer = BrightCyan,
    tertiary = EmeraldSecure,
    background = DeepObsidianNavy,
    onBackground = SlateTextPrimaryDark,
    surface = MidnightNavy,
    onSurface = SlateTextPrimaryDark,
    surfaceVariant = ElevatedNavyCard,
    onSurfaceVariant = SlateTextSecondaryDark,
    outline = SlateBorderDark,
    error = CrimsonAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = IndigoPrimaryLight,
    secondary = CyanAccentLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = EmeraldSecure,
    background = PorcelainBackground,
    onBackground = SlateTextPrimaryLight,
    surface = PorcelainSurface,
    onSurface = SlateTextPrimaryLight,
    surfaceVariant = PorcelainSurfaceVariant,
    onSurfaceVariant = SlateTextSecondaryLight,
    outline = SlateBorderLight,
    error = CrimsonAlert,
    onError = Color.White
)

@Composable
fun MozaffarVpnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val tokens = if (darkTheme) {
        VpnThemeTokens(
            isDark = true,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(DeepObsidianNavy, MidnightNavy, Color(0xFF1B1040))
            ),
            cardSurface = ElevatedNavyCard.copy(alpha = 0.88f),
            cardSurfaceElevated = ElevatedNavyCardAlt,
            cardBorder = SlateBorderDark,
            accentGradient = Brush.linearGradient(listOf(RoyalViolet, ElectricViolet)),
            connectedColor = EmeraldSecure,
            reconnectingColor = AmberReconnect,
            errorColor = CrimsonAlert,
            cyanAccent = BrightCyan
        )
    } else {
        VpnThemeTokens(
            isDark = false,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(PorcelainBackground, Color(0xFFECE9FD), Color(0xFFE2E8F8))
            ),
            cardSurface = PorcelainSurface,
            cardSurfaceElevated = PorcelainSurfaceVariant,
            cardBorder = SlateBorderLight,
            accentGradient = Brush.linearGradient(listOf(IndigoPrimaryLight, VioletSecondaryLight)),
            connectedColor = EmeraldSecure,
            reconnectingColor = AmberReconnect,
            errorColor = CrimsonAlert,
            cyanAccent = CyanAccentLight
        )
    }

    CompositionLocalProvider(LocalVpnTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
