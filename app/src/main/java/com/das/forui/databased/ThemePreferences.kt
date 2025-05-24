package com.das.forui.databased

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

object ThemePreferences {

    fun saveDarkMode(context: Context, isDarkMode: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", isDarkMode).apply()
    }

    @Composable
    fun loadDarkModeState(context: Context): MutableState<Boolean> {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isSystemDark = isSystemInDarkTheme()

        val darkModeState = remember { mutableStateOf(prefs.getBoolean("dark_mode", isSystemDark)) }

        DisposableEffect(prefs) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == "dark_mode") {
                    darkModeState.value = prefs.getBoolean("dark_mode", isSystemDark)
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)

            onDispose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

        return darkModeState
    }

}