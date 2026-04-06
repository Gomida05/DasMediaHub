package com.das.mediaHub.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.das.mediaHub.data.model.SavedVideosListData

class WatchHistory(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun insertNewVideo(
        videoId: String,
        title: String,
        videoDate: String,
        videoViewCount: String,
        videoChannelName: String,
        duration: String,
        channelThumbnail: String
    ): Boolean {
        val db = writableDatabase

        db.beginTransaction()
        return try {
            val existsCursor = db.rawQuery(
                "SELECT 1 FROM $FAVOURITE_TABLE_NAME WHERE video_id = ? LIMIT 1",
                arrayOf(videoId)
            )

            val exists = existsCursor.use { it.moveToFirst() }
            if (exists) {
                return false
            }

            val countCursor = db.rawQuery(
                "SELECT COUNT(*) FROM $FAVOURITE_TABLE_NAME",
                null
            )

            val count = countCursor.use {
                if (it.moveToFirst()) it.getInt(0) else 0
            }

            if (count >= 30) {
                db.execSQL(
                    """
                    DELETE FROM $FAVOURITE_TABLE_NAME
                    WHERE rowid IN (
                        SELECT rowid FROM $FAVOURITE_TABLE_NAME
                        ORDER BY rowid ASC
                        LIMIT 1
                    )
                    """.trimIndent()
                )
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
            db.setTransactionSuccessful()
            result != -1L
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getWatchedVideos(): List<SavedVideosListData> {
        val db = readableDatabase

        val query = """
            SELECT 
                video_id,
                title,
                viewNumber,
                videoDate,
                videoChannelName,
                duration,
                channelThumbnail
            FROM $FAVOURITE_TABLE_NAME
            ORDER BY rowid DESC
        """.trimIndent()

        return db.rawQuery(query, null).use { cursor ->
            val videoIdIndex = cursor.getColumnIndexOrThrow("video_id")
            val titleIndex = cursor.getColumnIndexOrThrow("title")
            val viewNumberIndex = cursor.getColumnIndexOrThrow("viewNumber")
            val videoDateIndex = cursor.getColumnIndexOrThrow("videoDate")
            val channelNameIndex = cursor.getColumnIndexOrThrow("videoChannelName")
            val durationIndex = cursor.getColumnIndexOrThrow("duration")
            val channelThumbnailIndex = cursor.getColumnIndexOrThrow("channelThumbnail")

            val list = mutableListOf<SavedVideosListData>()

            while (cursor.moveToNext()) {
                val videoId = cursor.getString(videoIdIndex)

                list.add(
                    SavedVideosListData(
                        title = cursor.getString(titleIndex),
                        watchUrl = videoId,
                        thumbnailUrl = "https://img.youtube.com/vi/$videoId/0.jpg",
                        views = cursor.getString(viewNumberIndex),
                        dateTime = cursor.getString(videoDateIndex),
                        duration = cursor.getString(durationIndex),
                        channelName = cursor.getString(channelNameIndex),
                        channelThumbnail = cursor.getString(channelThumbnailIndex)
                    )
                )
            }

            list
        }
    }

    fun deleteWatchUrl(url: String): Int {
        val db = writableDatabase
        return db.use { db ->
            db.delete(
                FAVOURITE_TABLE_NAME,
                "video_id = ?",
                arrayOf(url)
            )
        }
    }

    companion object {
        private const val FAVOURITE_TABLE_NAME = "Watched_Videos"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "history.db"

        private const val SQL_CREATE_ENTRIES = """
            CREATE TABLE IF NOT EXISTS Watched_Videos (
                video_id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                viewNumber TEXT NOT NULL,
                videoDate TEXT NOT NULL,
                videoChannelName TEXT NOT NULL,
                duration TEXT NOT NULL,
                channelThumbnail TEXT NOT NULL
            )
        """

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS Watched_Videos"
    }
}