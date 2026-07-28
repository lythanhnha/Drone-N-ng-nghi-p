package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightGreenAccent,
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF1E4D2B),
    onPrimaryContainer = Color(0xFFB9F6CA),
    secondary = MintTeal,
    onSecondary = Color.White,
    tertiary = DebtAmberDark,
    onTertiary = Color(0xFF4A2800),
    background = DarkBackground,
    onBackground = Color(0xFFE2E8E3),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E8E3),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC2D0C3),
    error = DebtRedDark,
    onError = Color(0xFF600004)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = PrimaryGreen,
    secondary = MintTeal,
    onSecondary = Color.White,
    tertiary = DebtAmberLight,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF1B1D1B),
    surface = SurfaceLight,
    onSurface = Color(0xFF1B1D1B),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF2E4D32),
    error = DebtRedLight,
    onError = Color.White
)

@Composable
fun DroneNongNghiepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent agricultural branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

