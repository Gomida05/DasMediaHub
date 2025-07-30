package com.das.mediaHub.data.databased

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class WatchHistory(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }
    }

    fun insertNewVideo(
        videoId: String, title: String, videoDate:String, videoViewCount:String,
        videoChannelName:String, duration:String, channelThumbnail: String
    ): Boolean{
        if(isWatchUrlExist(videoId)){
            return false
        }

        val db = this.writableDatabase
        val countQuery = "SELECT COUNT(*) FROM $FAVOURITE_TABLE_NAME"
        val cursor = db.rawQuery(countQuery, null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        // If there are more than 27 rows, delete the oldest
        if (count >= 30) {
            val deleteQuery = "DELETE FROM $FAVOURITE_TABLE_NAME WHERE rowid IN " +
                    "(SELECT rowid FROM $FAVOURITE_TABLE_NAME ORDER BY rowid ASC LIMIT 1)"
            db.execSQL(deleteQuery)
        }

        val contentValues = ContentValues().apply {
            put("video_id", videoId)
            put("title", title)
            put("viewNumber", videoViewCount)
            put("videoDate", videoDate)
            put("videoChannelName", videoChannelName)
            put("duration", duration)
            put("channelThumbnail", channelThumbnail)
        }



        val result = db.insert(FAVOURITE_TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }



    private fun isWatchUrlExist(url: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM Watched_Videos WHERE video_id = ?", arrayOf(url))

        val exists = cursor.count > 0
        cursor.close()
        db.close()

        return exists
    }

    fun getResults(): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $FAVOURITE_TABLE_NAME ORDER BY rowid DESC", null)
    }


    fun getVideoTitle(watchUrl: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT title FROM $FAVOURITE_TABLE_NAME WHERE video_id = ?",
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
                "SELECT viewNumber FROM $FAVOURITE_TABLE_NAME WHERE video_id = ?",
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
        } catch (_: Exception) {
//            MainActivity().alertUserError(e.message.toString())
            return null
        }
    }

    fun getVideoDate(videoId: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT videoDate FROM $FAVOURITE_TABLE_NAME WHERE video_id = ?",
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
            "SELECT videoChannelName FROM $FAVOURITE_TABLE_NAME WHERE video_id = ?",
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

    fun getChannelNameThumbnail(videoId: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT channelThumbnail FROM $FAVOURITE_TABLE_NAME WHERE video_id = ?",
            arrayOf(videoId)
        )

        var viewNumber: String? = null
        if (cursor.moveToFirst()) {
            // Get column index for viewNumber
            val viewNumberColumnIndex = cursor.getColumnIndex("channelThumbnail")

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
            "SELECT duration FROM $FAVOURITE_TABLE_NAME WHERE video_id = ?",
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
            FAVOURITE_TABLE_NAME,
            "video_id = ?",
            arrayOf(url)
        )

        db.close()
        return rowsDeleted
    }
    
    companion object {
        private const val FAVOURITE_TABLE_NAME = "Watched_Videos"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "history.db"
        private const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS Watched_Videos ( video_id TEXT PRIMARY KEY, title TEXT NOT NULL, viewNumber TEXT NOT NULL, videoDate TEXT NOT NULL, videoChannelName TEXT NOT NULL, duration TEXT NOT NULL, channelThumbnail TEXT NOT NULL) """

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS Watched_Videos"
    }

}