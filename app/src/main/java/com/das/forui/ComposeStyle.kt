package com.das.forui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTheme(content: @Composable () -> Unit) {
    val lightPrimary = Color.Black
    val lightPrimaryVariant = Color(0xFF000000)
    val lightSecondary = Color(0xFF03DAC5)

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
            color = if (isSystemInDarkTheme())
                whiteColor else lightPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = customFontFamily,
            color = if (isSystemInDarkTheme())
                whiteColor else lightPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp
        )
        // Define other text styles like h3, body1, body2, etc.
    )

    MaterialTheme(

        colorScheme = if (isSystemInDarkTheme()) {
            darkColorScheme(
                primary = whiteColor,
                primaryContainer = Color(0xFF000000),
                onPrimary = Color(0xFF000000),
                secondary = lightSecondary,

                )
        } else {
            lightColorScheme(
                primary = lightPrimary,
                primaryContainer = lightPrimaryVariant,
                onPrimary = Color(0xFFFFFFFF),
                secondary = lightSecondary,

            )
        },
        typography = customTypography,
        shapes = customShapes,
        content = content
    )
}