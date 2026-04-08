package com.das.downloader.data.local

import android.content.Context
import androidx.core.content.edit
import com.das.downloader.data.model.PathType

object PathPreferences {

    private const val PREFS_NAME = "AppPreferences"
    private const val AUDIO_KEY = "download_path1"
    private const val VIDEO_KEY = "download_path2"

    private const val DEFAULT_AUDIO_PATH = "/storage/emulated/0/Music/DasMediaHub"

    private const val DEFAULT_VIDEO_PATH = "/storage/emulated/0/Movies/DasMediaHub"

    fun getPrefsName() = PREFS_NAME

    fun updatePath(context: Context, pathType: PathType, newPath: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        when (pathType) {
            PathType.AUDIO -> prefs.edit { putString(AUDIO_KEY, newPath) }
            PathType.VIDEO -> prefs.edit { putString(VIDEO_KEY, newPath) }
        }
    }


    fun getAudioPath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(AUDIO_KEY, DEFAULT_AUDIO_PATH) ?: DEFAULT_AUDIO_PATH
    }

    fun getVideoPath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(VIDEO_KEY, DEFAULT_VIDEO_PATH) ?: DEFAULT_VIDEO_PATH
    }

}