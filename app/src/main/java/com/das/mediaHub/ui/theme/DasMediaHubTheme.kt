package com.das.mediaHub.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.das.mediaHub.ui.theme.ThemeContents.CustomDarkColors
import com.das.mediaHub.ui.theme.ThemeContents.CustomLightColors
import com.das.mediaHub.ui.theme.ThemePreferences.loadDarkModeState

@Composable
fun DasMediaHubTheme(
    content: @Composable () -> Unit
) {
    val themeState by loadDarkModeState()

    val isDarkTheme = when (themeState) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colors = if (isDarkTheme) CustomDarkColors else CustomLightColors

    // Modern shapes with larger corners
    val customShapes = Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(28.dp) // As used in our new Cards
    )

    MaterialTheme(
        colorScheme = colors,
        typography = ThemeContents.Typography,
        shapes = customShapes,
        content = content
    )
}
