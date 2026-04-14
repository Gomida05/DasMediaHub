package com.das.mediaHub.data.model

data class VideoItem(
    val title: String,
    val watchUrl: String,
    val views: String,
    val dateTime: String,
    val duration: String,
    val channelName: String,
    val channelThumbnail: String
) {
    val thumbnailUrl: String
        get() = "https://img.youtube.com/vi/$watchUrl/0.jpg"

    companion object {
        fun SavedVideosListData.toVideoItem(): VideoItem {
            return VideoItem(
                title = title,
                watchUrl = watchUrl,
                views = views,
                dateTime = dateTime,
                duration = duration,
                channelName = channelName,
                channelThumbnail = channelThumbnail
            )
        }

        fun WatchedVideoEntity.toVideoItem(): VideoItem {
            return VideoItem(
                title = title,
                watchUrl = watchUrl,
                views = views,
                dateTime = dateTime,
                duration = duration,
                channelName = channelName,
                channelThumbnail = channelThumbnail
            )
        }
    }
}