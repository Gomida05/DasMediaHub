package com.das.mediaHub.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit


class PathSaver(private val context: Context) {

    private val prefName = "AppPreferences"
    private val audioKey = "download_path1"
    private val videoKey = "download_path2"
    private val sharedPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun getAudioDownloadPath(): String {

        var downloadPath = sharedPref.getString(audioKey, null)

        if (downloadPath == null) {
            downloadPath = getMusicDefaultDownloadPath()
            sharedPref.edit {
                putString(audioKey, downloadPath)
            }
        }
        return downloadPath
    }

    private fun getMusicDefaultDownloadPath(): String {
        return "/storage/emulated/0/Music/DasMediaHub"
    }

    fun setAudioDownloadPath(path: String) {
        sharedPref.edit {
            // Replace the old path with the new one
            putString(audioKey, path)
            // Save the change asynchronously
        }
    }

    fun getVideosDownloadPath(): String {
        var downloadPath = sharedPref.getString(videoKey, null)

        if (downloadPath == null) {
            downloadPath = getMoviesDefaultDownloadPath()
            sharedPref.edit {
                putString(videoKey, downloadPath)
            }
        }

        return downloadPath
    }


    fun setMoviesDownloadPath(path: String) {
        val sharedPref: SharedPreferences =context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        sharedPref.edit {
            // Replace the old path with the new one
            putString(videoKey, path)
        }
    }

    private fun getMoviesDefaultDownloadPath(): String {
        return "/storage/emulated/0/Movies/DasMediaHub"
    }


}