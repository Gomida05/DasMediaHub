package com.das.downloader.data.local

import android.content.Context
import androidx.core.content.edit

object PathPreferences {

    private const val PREFS_NAME = "AppPreferences"
    private const val AUDIO_KEY = "download_path1"
    private const val VIDEO_KEY = "download_path2"

    private const val DEFAULT_AUDIO_PATH = "/storage/emulated/0/Music/DasMediaHub"

    private const val DEFAULT_VIDEO_PATH = "/storage/emulated/0/Movies/DasMediaHub"

    fun getPrefsName() = PREFS_NAME

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
        return prefs.getString(VIDEO_KEY, DEFAULT_VIDEO_PATH) ?: DEFAULT_VIDEO_PATH
    }

    enum class PathType(val label: String) {
        AUDIO(label = "download_path1"),
        VIDEO(label = "download_path2")
    }
}