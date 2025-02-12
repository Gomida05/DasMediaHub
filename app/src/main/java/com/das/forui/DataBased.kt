@file:Suppress("unused")

package com.das.forui

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

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



class DatabaseHelper1(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun getResults(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM results", null) }
    fun insertData(title: String, path: String): Boolean {
        if (isTitleExist(title)) {

            return false
        }
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("title", title)
            put("path", path)
        }
        val result = db.insert("results", null, contentValues)
        db.close()
        return result != -1L }
    private fun isTitleExist(title: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM results WHERE title = ?", arrayOf(title))

        val exists = cursor.count > 0 // If count > 0, title exists
        cursor.close() // Don't forget to close the cursor
        db.close() // Close the database connection
        return exists
    }
    private fun getSortedResults(): Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM results ORDER BY created_at DESC"
        return db.rawQuery(query, null)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "downloads_history.db"
        const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS results ( title TEXT PRIMARY KEY, path TEXT NOT NULL )"""

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS results"
    }

}

class SearchHistoryDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun getResults(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM results", null) }
    private fun isWatchUrlExist(url: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM results WHERE title = ?", arrayOf(url.let {
            it.trimEnd()
            it.trimStart()
        }))

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }



    fun deleteSearchList(selectedItem: String): Int {
        val db = this.writableDatabase

        try {
            // Log the trimmed item to ensure correct value
            Log.d("Database", "Attempting to delete item: '${selectedItem}'")

            val rowsDeleted = db.delete(
                "results",
                "title = ?",
                arrayOf(selectedItem)
            )

            Log.d("Database", "Rows deleted: $rowsDeleted")
            return rowsDeleted
        } catch (e: Exception) {
            Log.e("Database", "Error deleting item: ${e.message}")
            return 0
        } finally {
            db.close()
        }
    }




    fun insertData(title: String): Boolean {
        if(isWatchUrlExist(title)){
            return false
        }
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("title", title.let{
                it.trimEnd()
                it.trimStart()
            })
        }
        val result = db.insert("results", null, contentValues)
        db.close()
        return result != -1L
    }


    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "search_history.db"
        const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS results ( title TEXT PRIMARY KEY) """

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS results"
    }

}


class DatabaseFavorite(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun getResults(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM results", null) }


    fun isTableEmpty(): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM results LIMIT 1"
        val cursor = db.rawQuery(query, null)

        cursor.use {
            return !it.moveToFirst()
        }

    }

    fun isWatchUrlExist(url: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM results WHERE video_id = ?", arrayOf(url))

        val exists = cursor.count > 0
        cursor.close()
        db.close()

        return exists // Return true if URL exists, otherwise false
    }




    fun insertData(
        videoId: String, title: String, videoDate:String, videoViewCount:String,
        videoChannelName:String, duration:String): Boolean {
        if(isWatchUrlExist(videoId)){
            return false
        }
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("video_id", videoId)
            put("title", title)
            put("viewNumber", videoViewCount)
            put("videoDate", videoDate)
            put("videoChannelName", videoChannelName)
            put("duration", duration)
        }



        val result = db.insert("results", null, contentValues)
        db.close()
        return result != -1L
    }


    fun getVideoTitle(watchUrl: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT title FROM results WHERE video_id = ?",
            arrayOf(watchUrl)
        )

        var viewNumber: String? = null
        if (cursor.moveToFirst()) {
            // Get column index for viewNumber
            val viewNumberColumnIndex = cursor.getColumnIndex("title")

            // Check if the column index is valid (>= 0)
            if (viewNumberColumnIndex >= 0) {
                viewNumber = cursor.getString(viewNumberColumnIndex)
            } else {
                Log.e("Database", "Column 'viewNumber' not found.")
            }
        }
        cursor.close()
        db.close()
        return viewNumber
    }



    fun getViewNumber(videoId: String): String? {
        val db = this.readableDatabase
        try {
            val cursor = db.rawQuery(
                "SELECT viewNumber FROM results WHERE video_id = ?",
                arrayOf(videoId)
            )

            var viewNumber: String? = null
            if (cursor.moveToFirst()) {
                // Get column index for viewNumber
                val viewNumberColumnIndex = cursor.getColumnIndex("viewNumber")

                // Check if the column index is valid (>= 0)
                if (viewNumberColumnIndex >= 0) {
                    viewNumber = cursor.getString(viewNumberColumnIndex)
                } else {
                    Log.e("Database", "Column 'viewNumber' not found.")
                }
            }
            cursor.close()
            db.close()
            return viewNumber
        } catch (e: Exception) {
//            MainActivity().alertUserError(e.message.toString())
            return null
        }
    }

    fun getVideoDate(videoId: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT videoDate FROM results WHERE video_id = ?",
            arrayOf(videoId)
        )

        var viewNumber: String? = null
        if (cursor.moveToFirst()) {
            // Get column index for viewNumber
            val viewNumberColumnIndex = cursor.getColumnIndex("videoDate")

            // Check if the column index is valid (>= 0)
            if (viewNumberColumnIndex >= 0) {
                viewNumber = cursor.getString(viewNumberColumnIndex)
            } else {
                Log.e("Database", "Column 'viewNumber' not found.")
            }
        }
        cursor.close()
        db.close()
        return viewNumber
    }


    fun getVideoChannelName(videoId: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT videoChannelName FROM results WHERE video_id = ?",
            arrayOf(videoId)
        )

        var viewNumber: String? = null
        if (cursor.moveToFirst()) {
            // Get column index for viewNumber
            val viewNumberColumnIndex = cursor.getColumnIndex("videoChannelName")

            // Check if the column index is valid (>= 0)
            if (viewNumberColumnIndex >= 0) {
                viewNumber = cursor.getString(viewNumberColumnIndex)
            } else {
                Log.e("Database", "Column 'viewNumber' not found.")
            }
        }
        cursor.close()
        db.close()
        return viewNumber
    }

    fun getDuration(videoId: String): String?{
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT duration FROM results WHERE video_id = ?",
            arrayOf(videoId)
        )

        var duration: String? = null
        if (cursor.moveToFirst()) {
            // Get column index for viewNumber
            val durationColumnIndex = cursor.getColumnIndex("duration")

            // Check if the column index is valid (>= 0)
            if (durationColumnIndex >= 0) {
                duration = cursor.getString(durationColumnIndex)
            } else {
                Log.e("Database", "Column 'duration' not found.")
            }
        }
        cursor.close()
        db.close()
        return duration
    }


    fun deleteWatchUrl(url: String): Int {
        val db = this.writableDatabase


        val rowsDeleted = db.delete(
            "results",
            "video_id = ?",
            arrayOf(url)
        )

        db.close()
        return rowsDeleted
    }
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "favorites.db"
        const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS results ( video_id TEXT PRIMARY KEY, title TEXT NOT NULL, viewNumber TEXT NOT NULL, videoDate TEXT NOT NULL, videoChannelName TEXT NOT NULL, duration TEXT NOT NULL) """

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS results"
    }

}