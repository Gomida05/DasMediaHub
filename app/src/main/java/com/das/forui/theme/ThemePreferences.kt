package com.das.forui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.edit

object ThemePreferences {

    private const val PREFS_NAME = "app_prefs"
    private const val THEME_KEY = "app_theme"
    fun saveDarkMode(context: Context, theme: AppTheme) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(THEME_KEY, theme.name) }
    }
    @Composable
    fun loadDarkModeState(context: Context): MutableState<AppTheme> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(THEME_KEY, AppTheme.SYSTEM.name)
        val themeState = remember { mutableStateOf(AppTheme.valueOf(savedTheme ?: AppTheme.SYSTEM.name)) }

        DisposableEffect(prefs) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == THEME_KEY) {
                    val updatedTheme = prefs.getString(THEME_KEY, AppTheme.SYSTEM.name)
                    themeState.value = AppTheme.valueOf(updatedTheme ?: AppTheme.SYSTEM.name)
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)

            onDispose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

        return themeState
    }

}