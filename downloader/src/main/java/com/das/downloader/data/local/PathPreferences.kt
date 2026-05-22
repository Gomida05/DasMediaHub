package com.das.downloader.data.local

import android.content.Context
import androidx.core.content.edit
import com.das.downloader.data.model.PathType

/**
 * Utility object for managing user-defined storage paths for downloads.
 * 
 * It stores the base directories for audio and video files in [android.content.SharedPreferences].
 * 
 * Example usage:
 * ```kotlin
 * val musicDir = PathPreferences.getAudioPath(context)
 * PathPreferences.updatePath(context, PathType.VIDEO, "/sdcard/Downloads/Movies")
 * ```
 */
object PathPreferences {

    private const val PREFS_NAME = "AppPreferences"
    private const val AUDIO_KEY = "download_path1"
    private const val VIDEO_KEY = "download_path2"

    private const val DEFAULT_AUDIO_PATH = "/storage/emulated/0/Music/DasMediaHub"
    private const val DEFAULT_VIDEO_PATH = "/storage/emulated/0/Movies/DasMediaHub"

    /**
     * Returns the name of the SharedPreferences file used.
     */
    fun getPrefsName() = PREFS_NAME

    /**
     * Updates the storage path for a specific media type.
     * 
     * @param context Android context.
     * @param pathType The type of path to update (AUDIO or VIDEO).
     * @param newPath The absolute path to the new directory.
     */
    fun updatePath(context: Context, pathType: PathType, newPath: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        when (pathType) {
            PathType.AUDIO -> prefs.edit { putString(AUDIO_KEY, newPath) }
            PathType.VIDEO -> prefs.edit { putString(VIDEO_KEY, newPath) }
        }
    }

    /**
     * Retrieves the current base directory for audio downloads.
     * @return The absolute path string.
     */
    fun getAudioPath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(AUDIO_KEY, DEFAULT_AUDIO_PATH) ?: DEFAULT_AUDIO_PATH
    }

    /**
     * Retrieves the current base directory for video downloads.
     * @return The absolute path string.
     */
    fun getVideoPath(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(VIDEO_KEY, DEFAULT_VIDEO_PATH) ?: DEFAULT_VIDEO_PATH
    }

}
