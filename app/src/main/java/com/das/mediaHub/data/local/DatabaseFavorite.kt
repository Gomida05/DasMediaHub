package com.das.mediaHub.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.das.mediaHub.data.model.SavedVideosListData

class DatabaseFavorite(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun getAllSavedVideos(): List<SavedVideosListData> {
        val db = readableDatabase
        val result = mutableListOf<SavedVideosListData>()

        db.query(
            TABLE_NAME,
            arrayOf(
                COL_VIDEO_ID,
                COL_TITLE,
                COL_VIEW_NUMBER,
                COL_VIDEO_DATE,
                COL_CHANNEL_NAME,
                COL_DURATION,
                COL_CHANNEL_THUMBNAIL
            ),
            null,
            null,
            null,
            null,
            "rowid DESC"
        ).use { cursor ->
            val videoIdIndex = cursor.getColumnIndexOrThrow(COL_VIDEO_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(COL_TITLE)
            val viewNumberIndex = cursor.getColumnIndexOrThrow(COL_VIEW_NUMBER)
            val videoDateIndex = cursor.getColumnIndexOrThrow(COL_VIDEO_DATE)
            val channelNameIndex = cursor.getColumnIndexOrThrow(COL_CHANNEL_NAME)
            val durationIndex = cursor.getColumnIndexOrThrow(COL_DURATION)
            val channelThumbnailIndex = cursor.getColumnIndexOrThrow(COL_CHANNEL_THUMBNAIL)

            while (cursor.moveToNext()) {
                val videoId = cursor.getString(videoIdIndex)

                result.add(
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
        }

        return result
    }

    fun isWatchUrlExist(videoId: String): Boolean {
        val db = readableDatabase
        db.rawQuery(
            "SELECT 1 FROM $TABLE_NAME WHERE $COL_VIDEO_ID = ? LIMIT 1",
            arrayOf(videoId)
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    fun insertData(
        videoId: String,
        title: String,
        videoDate: String,
        videoViewCount: String,
        videoChannelName: String,
        duration: String,
        channelThumbnail: String
    ): Boolean {
        if (isWatchUrlExist(videoId)) return false

        val values = ContentValues().apply {
            put(COL_VIDEO_ID, videoId)
            put(COL_TITLE, title)
            put(COL_VIEW_NUMBER, videoViewCount)
            put(COL_VIDEO_DATE, videoDate)
            put(COL_CHANNEL_NAME, videoChannelName)
            put(COL_DURATION, duration)
            put(COL_CHANNEL_THUMBNAIL, channelThumbnail)
        }

        return writableDatabase.insert(TABLE_NAME, null, values) != -1L
    }

    fun deleteWatchUrl(videoId: String): Int {
        return writableDatabase.delete(
            TABLE_NAME,
            "$COL_VIDEO_ID = ?",
            arrayOf(videoId)
        )
    }

    companion object {
        private const val TABLE_NAME = "Saved_for_later"
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "favorites.db"

        private const val COL_VIDEO_ID = "video_id"
        private const val COL_TITLE = "title"
        private const val COL_VIEW_NUMBER = "viewNumber"
        private const val COL_VIDEO_DATE = "videoDate"
        private const val COL_CHANNEL_NAME = "videoChannelName"
        private const val COL_DURATION = "duration"
        private const val COL_CHANNEL_THUMBNAIL = "channelThumbnail"

        private const val SQL_CREATE_ENTRIES = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_VIDEO_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT NOT NULL,
                $COL_VIEW_NUMBER TEXT NOT NULL,
                $COL_VIDEO_DATE TEXT NOT NULL,
                $COL_CHANNEL_NAME TEXT NOT NULL,
                $COL_DURATION TEXT NOT NULL,
                $COL_CHANNEL_THUMBNAIL TEXT NOT NULL
            )
        """

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}