package com.das.forui.databased

import android.content.Context
import android.content.SharedPreferences


class PathSaver {

    fun getMusicDownloadPath(context: Context): String? {

        val sharedPref: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        var downloadPath = sharedPref.getString("download_path1", null)

        if (downloadPath == null) {
            downloadPath = getMusicDefaultDownloadPath()
            val editor = sharedPref.edit()
            editor.putString("download_path1", downloadPath)
            editor.apply()
        }
        return downloadPath
    }

    private fun getMusicDefaultDownloadPath(): String {
        return "/storage/emulated/0/Music/ForUI"
    }

    fun setMusicDownloadPath(context: Context, path: String) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            // Replace the old path with the new one
            putString("download_path1", path)
            apply()  // Save the change asynchronously
        }
    }

    fun getVideosDownloadPath(context: Context): String? {

        val sharedPref: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        var downloadPath = sharedPref.getString("download_path2", null)

        if (downloadPath == null) {
            downloadPath = getMoviesDefaultDownloadPath()
            val editor = sharedPref.edit()
            editor.putString("download_path2", downloadPath)
            editor.apply()
        }
        return downloadPath
    }


    fun setMoviesDownloadPath(context: Context, path: String) {
        println("hello there\n$path")
        val sharedPref: SharedPreferences =context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            // Replace the old path with the new one
            putString("download_path2", path)
            apply()  // Save the change asynchronously
        }
    }

    private fun getMoviesDefaultDownloadPath(): String {
        return "/storage/emulated/0/Movies/ForUI"
    }
}