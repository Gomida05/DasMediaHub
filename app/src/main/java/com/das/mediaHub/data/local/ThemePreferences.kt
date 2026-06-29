package com.das.mediaHub.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.platform.LocalContext
import com.das.downloader.data.local.DownloadPreferences
import com.das.downloader.data.local.DownloadPreferences.getPrefsName
import com.das.downloader.data.model.PathType

/**
 * Utility object for accessing and observing storage path preferences within Composable functions.
 *
 * It provides reactive [MutableState] wrappers around [DownloadPreferences] to allow UI 
 * components to automatically recompose when a download path is updated in settings.
 */
internal object ThemePreferences {

    /**
     * Returns a reactive state for the audio download path.
     * @return [MutableState] containing the current audio path string.
     */
    @Composable
    fun audioPathState(): MutableState<String> {
        return rememberPathState(key = PathType.AUDIO)
    }

    /**
     * Returns a reactive state for the video download path.
     * @return [MutableState] containing the current video path string.
     */
    @Composable
    fun videoPathState(): MutableState<String> {
        return rememberPathState(key = PathType.VIDEO)
    }

    /**
     * Internal helper to create a reactive state that listens to SharedPreferences changes.
     * 
     * @param key The [PathType] representing the preference to observe.
     * @return A [MutableState] that stays in sync with the underlying preference.
     */
    @Composable
    private fun rememberPathState(
        key: PathType
    ): MutableState<String> {

        val context = LocalContext.current

        // Helper to fetch the latest value based on PathType
        fun getCurrentPath(): String = if (key == PathType.AUDIO) {
            DownloadPreferences.getAudioPath(context)
        } else {
            DownloadPreferences.getVideoPath(context)
        }

        val prefs = retain {
            context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        }

        val state = retain {
            mutableStateOf(getCurrentPath())
        }

        RetainedEffect (Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key.label) {
                    state.value = getCurrentPath()
                }
            }

            prefs.registerOnSharedPreferenceChangeListener(listener)
            onRetire {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

        return state
    }
}
