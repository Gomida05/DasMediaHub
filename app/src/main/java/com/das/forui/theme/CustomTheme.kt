package com.das.forui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.das.forui.theme.ThemePreferences.loadDarkModeState

@Composable
fun CustomTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val themeState by loadDarkModeState(context)

    val isDarkTheme = when (themeState) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val lightColors = lightColorScheme(
        primary = Color(0xFF000000),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0E0E0),
        secondary = Color(0xFF03DAC5),
        onSecondary = Color.Black,
        background = Color(0xFFFFFFFF),
        onBackground = Color.Black,
        surface = Color(0xFFFFFFFF),
        onSurface = Color.Black,
    )

    val darkColors = darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF121212),
        secondary = Color(0xFF03DAC5),
        onSecondary = Color.Black,
        background = Color(0xFF121212),
        onBackground = Color.White,
        surface = Color(0xFF121212),
        onSurface = Color.White,
    )

    val colors = if (isDarkTheme) darkColors else lightColors

    val lightPrimary = Color.Black

    val whiteColor = Color(0xFFFFFFFF)

    val customShapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(6.dp),
        large = RoundedCornerShape(8.dp)
    )
    val customFontFamily = FontFamily.Default

    val customTypography = Typography(
        headlineLarge = TextStyle(
            fontFamily = customFontFamily,
            color = if (isDarkTheme)
                whiteColor else lightPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = customFontFamily,
            color = if (isDarkTheme)
                whiteColor else lightPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = customFontFamily,
            color = if (isDarkTheme)
                whiteColor else lightPrimary,
            fontWeight = FontWeight.Normal,
            fontSize = 19.sp
        )
        // Define other text styles like h3, body1, body2, etc.
    )

    MaterialTheme(

        colorScheme = colors,
        typography = customTypography,
        shapes = customShapes,
        content = content
    )
}