package com.das.mediaHub.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

internal object ThemePreferences {

    private const val PREFS_NAME = "app_prefs"
    private const val THEME_KEY = "app_theme"
    fun saveDarkMode(context: Context, theme: AppTheme) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(THEME_KEY, theme.name) }
    }
    @Composable
    fun loadDarkModeState(): MutableState<AppTheme> {
        val context = LocalContext.current
        val prefs = remember {
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
        val savedTheme = prefs.getString(
            THEME_KEY,
            AppTheme.SYSTEM.name
        )

        val themeState = remember { mutableStateOf(safeTheme(savedTheme)) }

        DisposableEffect(Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == THEME_KEY) {
                    themeState.value = safeTheme(
                        prefs.getString(
                            THEME_KEY,
                            AppTheme.SYSTEM.name
                        )
                    )
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

        return themeState
    }

    private fun safeTheme(value: String?): AppTheme {
        return try {
            AppTheme.valueOf(value ?: AppTheme.SYSTEM.name)
        } catch (_: Exception) {
            AppTheme.SYSTEM
        }
    }
}