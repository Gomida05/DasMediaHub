package com.das.mediaHub.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.ui.platform.LocalContext
import com.das.downloader.data.local.PathPreferences
import com.das.downloader.data.local.PathPreferences.getPrefsName

internal object Preferences {



    @Composable
    fun audioPathState(): MutableState<String> {
        return rememberPathState(
            key = PathPreferences.PathType.AUDIO
        )
    }

    @Composable
    fun videoPathState(): MutableState<String> {
        return rememberPathState(
            key = PathPreferences.PathType.VIDEO
        )
    }

    @Composable
    private fun rememberPathState(
        key: PathPreferences.PathType
    ): MutableState<String> {

        val context = LocalContext.current

        val path = remember {
            if (key == PathPreferences.PathType.AUDIO) {
                PathPreferences.getAudioPath(context)
            } else {
                PathPreferences.getVideoPath(context)
            }
        }


        val prefs = remember {
            context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        }

        val state = remember {
            mutableStateOf(path)
        }

        RetainedEffect (Unit) {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
                    if (changedKey == key.label) {
                        state.value = path
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
