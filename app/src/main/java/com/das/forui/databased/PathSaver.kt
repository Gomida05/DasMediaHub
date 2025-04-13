package com.das.forui.databased

import android.content.Context
import android.content.SharedPreferences


object PathSaver {

    private const val AUDIO_KEY = "download_path1"
    private const val VIDEO_KEY = "download_path2"
    private const val PREF_NAME = "AppPreferences"

    fun getAudioDownloadPath(context: Context): String {

        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var downloadPath = sharedPref.getString(AUDIO_KEY, null)

        if (downloadPath == null) {
            downloadPath = getMusicDefaultDownloadPath()
            val editor = sharedPref.edit()
            editor.putString(AUDIO_KEY, downloadPath)
            editor.apply()
        }
        println("here is the path\n$downloadPath")
        return downloadPath.toString()
    }

    private fun getMusicDefaultDownloadPath(): String {
        return "/storage/emulated/0/Music/ForUI"
    }

    fun setAudioDownloadPath(context: Context, path: String) {
        println("Here is new saved path: $path")
        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            // Replace the old path with the new one
            putString(AUDIO_KEY, path)
            apply()  // Save the change asynchronously
        }
    }

    fun getVideosDownloadPath(context: Context): String {

        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var downloadPath = sharedPref.getString(VIDEO_KEY, null)

        if (downloadPath == null) {
            downloadPath = getMoviesDefaultDownloadPath()
            val editor = sharedPref.edit()
            editor.putString(VIDEO_KEY, downloadPath)
            editor.apply()
        }
        println("here is the path\n$downloadPath")
        return downloadPath.toString()
    }


    fun setMoviesDownloadPath(context: Context, path: String) {
        println("hello there\n$path")
        println("Here is new saved path: $path")
        val sharedPref: SharedPreferences =context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            // Replace the old path with the new one
            putString(VIDEO_KEY, path)
            apply()  // Save the change asynchronously
        }
    }

    private fun getMoviesDefaultDownloadPath(): String {
        return "/storage/emulated/0/Movies/ForUI"
    }
}