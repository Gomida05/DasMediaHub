package com.das.mediaHub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.das.mediaHub.ui.theme.ThemeContents.CustomDarkColors
import com.das.mediaHub.ui.theme.ThemeContents.CustomLightColors
import com.das.mediaHub.ui.theme.ThemePreferences.loadThemeState

@Composable
fun DasMediaHubTheme(
    content: @Composable () -> Unit
) {
    val themeState by loadThemeState()
    val context = LocalContext.current

    val isDarkTheme = when (themeState) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkTheme -> CustomDarkColors
        else -> CustomLightColors
    }
    // Modern shapes with larger corners
    val customShapes = Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(28.dp) // As used in our new Cards
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ThemeContents.Typography,
        shapes = customShapes,
        content = content
    )
}
