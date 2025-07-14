package com.das.forui.data.databased.room.dataclass

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Saved_for_later")
data class VideoForRoom(
    @PrimaryKey val videoId: String,
    val title: String,
    val views: String,
    val dateOfVideo: String,
    val duration: String,
    val channelName: String,
    val channelThumbnailsUrl: String
)