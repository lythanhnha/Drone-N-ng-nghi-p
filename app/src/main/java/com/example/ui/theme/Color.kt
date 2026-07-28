package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryGreen = Color(0xFF1B5E20)
val SecondaryGreen = Color(0xFF2E7D32)
val LightGreenAccent = Color(0xFF81C784)
val MintTeal = Color(0xFF00897B)

// Status colors - Light
val DebtRedLight = Color(0xFFC62828)
val DebtAmberLight = Color(0xFFE65100)
val PaidGreenLight = Color(0xFF2E7D32)

// Status colors - Dark
val DebtRedDark = Color(0xFFFF6B6B)
val DebtAmberDark = Color(0xFFFFB74D)
val PaidGreenDark = Color(0xFF66BB6A)

// Dynamic status colors based on theme
val DebtRed: Color
    @Composable get() = if (isSystemInDarkTheme()) DebtRedDark else DebtRedLight

val DebtAmber: Color
    @Composable get() = if (isSystemInDarkTheme()) DebtAmberDark else DebtAmberLight

val PaidGreen: Color
    @Composable get() = if (isSystemInDarkTheme()) PaidGreenDark else PaidGreenLight

val BackgroundLight = Color(0xFFF3F7F4)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE8F5E9)

val DarkBackground = Color(0xFF101912)
val DarkSurface = Color(0xFF1B261D)
val DarkSurfaceVariant = Color(0xFF253528)


