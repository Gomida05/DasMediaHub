package com.das.mediaHub.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object AppMigrations {

    // Search DB
    val SEARCH_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE search_data_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    value TEXT NOT NULL
                )
            """)

            db.execSQL("""
                INSERT INTO search_data_new (id, value)
                SELECT id, value FROM search_data
            """)

            db.execSQL("DROP TABLE search_data")
            db.execSQL("ALTER TABLE search_data_new RENAME TO search_data")
        }
    }

    // Favorites DB
    val FAVORITES_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS Saved_for_later_new (
                    video_id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    viewNumber TEXT NOT NULL,
                    videoDate TEXT NOT NULL,
                    videoChannelName TEXT NOT NULL,
                    duration TEXT NOT NULL,
                    channelThumbnail TEXT NOT NULL
                )
            """)

            db.execSQL("""
                INSERT INTO Saved_for_later_new (
                    video_id,
                    title,
                    viewNumber,
                    videoDate,
                    videoChannelName,
                    duration,
                    channelThumbnail
                )
                SELECT
                    video_id,
                    title,
                    viewNumber,
                    videoDate,
                    videoChannelName,
                    duration,
                    channelThumbnail
                FROM Saved_for_later
            """)

            db.execSQL("DROP TABLE Saved_for_later")
            db.execSQL("ALTER TABLE Saved_for_later_new RENAME TO Saved_for_later")
        }
    }

    // Watch History DB
    val HISTORY_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS Watched_Videos_new (
                    video_id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    viewNumber TEXT NOT NULL,
                    videoDate TEXT NOT NULL,
                    videoChannelName TEXT NOT NULL,
                    duration TEXT NOT NULL,
                    channelThumbnail TEXT NOT NULL
                )
            """)

            db.execSQL("""
                INSERT INTO Watched_Videos_new (
                    video_id,
                    title,
                    viewNumber,
                    videoDate,
                    videoChannelName,
                    duration,
                    channelThumbnail
                )
                SELECT
                    video_id,
                    title,
                    viewNumber,
                    videoDate,
                    videoChannelName,
                    duration,
                    channelThumbnail
                FROM Watched_Videos
            """)

            db.execSQL("DROP TABLE Watched_Videos")
            db.execSQL("ALTER TABLE Watched_Videos_new RENAME TO Watched_Videos")
        }
    }
}