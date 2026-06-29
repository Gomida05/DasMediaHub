package com.das.downloader.data.local

import android.content.Context
import androidx.core.content.edit
import com.das.downloader.data.model.PathType

/**
 * Utility object for managing user-defined download preferences in [android.content.SharedPreferences].
 *
 * This includes storage paths, concurrency limits, and network rules.
 *
 * Example usage:
 * ```kotlin
 * val maxDownloads = DownloadPreferences.getMaxConcurrentDownloads(context)
 * DownloadPreferences.updateMaxConcurrentDownloads(context, 5)
 * ```
 */
object DownloadPreferences {

    private const val PREFS_NAME = "AppPreferences"
    private const val AUDIO_KEY = "download_path1"
    private const val VIDEO_KEY = "download_path2"
    private const val MAX_CONCURRENT_KEY = "max_concurrent_downloads"
    private const val DOWNLOAD_OVER_DATA_KEY = "download_over_mobile_data"

    private const val DEFAULT_AUDIO_PATH = "/storage/emulated/0/Music/DasMediaHub"
    private const val DEFAULT_VIDEO_PATH = "/storage/emulated/0/Movies/DasMediaHub"
    private const val DEFAULT_MAX_CONCURRENT = 3
    private const val DEFAULT_DOWNLOAD_OVER_DATA = false

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

    /**
     * Updates the maximum number of concurrent downloads allowed.
     *
     * @param context Android context.
     * @param max The maximum number of concurrent downloads.
     */
    fun updateMaxConcurrentDownloads(context: Context, max: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(MAX_CONCURRENT_KEY, max) }
    }

    /**
     * Retrieves the maximum number of concurrent downloads allowed.
     * @return The integer limit.
     */
    fun getMaxConcurrentDownloads(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(MAX_CONCURRENT_KEY, DEFAULT_MAX_CONCURRENT)
    }

    /**
     * Updates whether downloads are allowed over mobile data.
     *
     * @param context Android context.
     * @param enabled True if downloads over mobile data are allowed.
     */
    fun updateDownloadOverMobileData(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(DOWNLOAD_OVER_DATA_KEY, enabled) }
    }

    /**
     * Retrieves whether downloads are allowed over mobile data.
     * @return True if allowed.
     */
    fun getDownloadOverMobileData(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(DOWNLOAD_OVER_DATA_KEY, DEFAULT_DOWNLOAD_OVER_DATA)
    }

}
