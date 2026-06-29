package com.das.mediaHub.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object ThemeContents {

    val CustomLightColors = lightColorScheme(
        primary = Color(0xFF006769), // Modern Soft Teal
        onPrimary = Color.White,
        primaryContainer = Color(0xFFC7F0F0),
        onPrimaryContainer = Color(0xFF002021),
        secondary = Color(0xFF904D00), // Modern Amber
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFDCC7),
        onSecondaryContainer = Color(0xFF2E1500),
        background = Color(0xFFF6FBF9), // Very light tinted background
        onBackground = Color(0xFF171D1D),
        surface = Color(0xFFFCFDFD),
        onSurface = Color(0xFF171D1D),
        surfaceVariant = Color(0xFFDBE5E5), // Softer gray-teal
        onSurfaceVariant = Color(0xFF3F4948),
        outline = Color(0xFF6F7979),
        error = Color(0xFFBA1A1A),
        outlineVariant = Color(0xFFBFC8C8)
    )

    val CustomDarkColors = darkColorScheme(
        primary = Color(0xFF4DB6AC), // Brighter teal for dark mode
        onPrimary = Color(0xFF003738),
        primaryContainer = Color(0xFF004F50),
        onPrimaryContainer = Color(0xFFC7F0F0),
        secondary = Color(0xFFFFB74D),
        onSecondary = Color(0xFF4D2600),
        secondaryContainer = Color(0xFF6E3900),
        onSecondaryContainer = Color(0xFFFFDCC7),
        background = Color(0xFF0E1415), // Deep dark teal/gray
        onBackground = Color(0xFFDEE3E3),
        surface = Color(0xFF0E1415),
        onSurface = Color(0xFFDEE3E3),
        surfaceVariant = Color(0xFF3F4948),
        onSurfaceVariant = Color(0xFFBFC8C8),
        outline = Color(0xFF899392),
        error = Color(0xFFFFB4AB)
    )

    val Typography = Typography(
        headlineLarge = TextStyle(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp
        ),
        headlineMedium = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        headlineSmall = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        bodyLarge = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 1.sp
        ),
        labelMedium = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    )
}