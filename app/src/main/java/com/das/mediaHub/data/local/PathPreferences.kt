package com.das.mediaHub.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

internal object PathPreferences {

    private const val PREFS_NAME = "AppPreferences"
    private const val AUDIO_KEY = "download_path1"
    private const val VIDEO_KEY = "download_path2"

    private const val DEFAULT_AUDIO_PATH = "/storage/emulated/0/Music/DasMediaHub"

    private const val DEFAULT_VIDEO_PATH = "/storage/emulated/0/Movies/DasMediaHub"

    fun saveAudioPath(context: Context, path: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(AUDIO_KEY, path) }
    }

    fun saveVideoPath(context: Context, path: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(VIDEO_KEY, path) }
    }

    fun getAudioPath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(AUDIO_KEY, DEFAULT_AUDIO_PATH) ?: DEFAULT_AUDIO_PATH
    }
    fun getVideoPath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(AUDIO_KEY, DEFAULT_AUDIO_PATH) ?: DEFAULT_AUDIO_PATH
    }

    @Composable
    fun audioPathState(): MutableState<String> {
        return rememberPathState(
            key = AUDIO_KEY,
            defaultValue = DEFAULT_AUDIO_PATH
        )
    }

    @Composable
    fun videoPathState(): MutableState<String> {
        return rememberPathState(
            key = VIDEO_KEY,
            defaultValue = DEFAULT_VIDEO_PATH
        )
    }

    @Composable
    private fun rememberPathState(
        key: String,
        defaultValue: String
    ): MutableState<String> {

        val context = LocalContext.current

        val prefs = remember {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        val state = remember {
            mutableStateOf(
                prefs.getString(key, defaultValue) ?: defaultValue
            )
        }

        // Ensure default is persisted once
        LaunchedEffect(Unit) {
            if (!prefs.contains(key)) {
                prefs.edit { putString(key, defaultValue) }
            }
        }

        DisposableEffect(Unit) {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                    if (changedKey == key) {
                        state.value =
                            prefs.getString(key, defaultValue) ?: defaultValue
                    }
                }

            prefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

        return state
    }
}
