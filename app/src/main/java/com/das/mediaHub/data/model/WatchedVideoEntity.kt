package com.das.mediaHub.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Watched_Videos")
data class WatchedVideoEntity(
    @ColumnInfo(name = "title")
    val title: String,

    @PrimaryKey
    @ColumnInfo(name = "video_id")
    val watchUrl: String,

    @ColumnInfo(name = "viewNumber")
    val views: String,

    @ColumnInfo(name = "videoDate")
    val dateTime: String,

    @ColumnInfo(name = "duration")
    val duration: String,

    @ColumnInfo(name = "videoChannelName")
    val channelName: String,

    @ColumnInfo(name = "channelThumbnail")
    val channelThumbnail: String
) {
    @get:Ignore
    val thumbnailUrl: String
        get() = "https://img.youtube.com/vi/$watchUrl/0.jpg"
}