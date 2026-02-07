package com.das.mediaHub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.das.mediaHub.ui.theme.ThemePreferences.loadDarkModeState

@Composable
fun CustomTheme(
    content: @Composable () -> Unit
) {
    val themeState by loadDarkModeState()

    val isDarkTheme = when (themeState) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    // Modern and unique Light Color Scheme
    val customLightColors = lightColorScheme(
        primary = Color(0xFF006064), // Deep Cyan
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0F7FA),
        onPrimaryContainer = Color(0xFF001F20),
        secondary = Color(0xFFFF7043), // Deep Orange
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFE0B2),
        onSecondaryContainer = Color(0xFF3E2723),
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF1A1C1E),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFE1E2E1),
        onSurfaceVariant = Color(0xFF444746),
        outline = Color(0xFF747775),
        error = Color(0xFFB00020)
    )

    // Modern and unique Dark Color Scheme
    val customDarkColors = darkColorScheme(
        primary = Color(0xFF80DEEA), // Light Cyan
        onPrimary = Color(0xFF00363A),
        primaryContainer = Color(0xFF004D40),
        onPrimaryContainer = Color(0xFFE0F7FA),
        secondary = Color(0xFFFFAB91), // Light Orange
        onSecondary = Color(0xFF4E2600),
        secondaryContainer = Color(0xFF6D4C41),
        onSecondaryContainer = Color(0xFFFFE0B2),
        background = Color(0xFF121212),
        onBackground = Color(0xFFE1E2E1),
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE1E2E1),
        surfaceVariant = Color(0xFF444746),
        onSurfaceVariant = Color(0xFFC4C7C5),
        outline = Color(0xFF8E918F),
        error = Color(0xFFF2B8B5)
    )

    val colors = if (isDarkTheme) customDarkColors else customLightColors

    // Expanded Typography for better hierarchy
    val customTypography = Typography(
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

    // Modern shapes with larger corners
    val customShapes = Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(28.dp) // As used in our new Cards
    )

    MaterialTheme(
        colorScheme = colors,
        typography = customTypography,
        shapes = customShapes,
        content = content
    )
}
