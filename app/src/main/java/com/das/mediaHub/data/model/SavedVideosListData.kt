package com.das.mediaHub.data.model

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Ignore
import androidx.room3.PrimaryKey

/**
 * Room [Entity] representing a video saved for later by the user.
 *
 * This class maps directly to the "Saved_for_later" table in the database and 
 * implements [VideoUiModel] for easy UI integration.
 *
 * @property title Title of the video.
 * @property videoId Unique identifier for the video (Primary Key).
 * @property views Formatted view count.
 * @property dateTime Formatted upload date.
 * @property duration Formatted video length.
 * @property channelName Name of the uploading channel.
 * @property channelThumbnail URL of the channel's profile picture.
 */
@Entity(tableName = "Saved_for_later")
data class SavedVideosListData(
    @ColumnInfo(name = "title")
    override val title: String,

    @PrimaryKey
    @ColumnInfo(name = "video_id")
    override val videoId: String,

    @ColumnInfo(name = "viewNumber")
    override val views: String,

    @ColumnInfo(name = "videoDate")
    override val dateTime: String,

    @ColumnInfo(name = "duration")
    override val duration: String,

    @ColumnInfo(name = "videoChannelName")
    override val channelName: String,

    @ColumnInfo(name = "channelThumbnail")
    override val channelThumbnail: String
) : VideoUiModel {

    /**
     * Dynamically generates the YouTube thumbnail URL for this video.
     */
    @get:Ignore
    override val thumbnailUrl: String
        get() = "https://img.youtube.com/vi/$videoId/0.jpg"
}
