package com.das.forui.objectsAndData


data class VideosListData(
    val videoId: String,
    val title: String,
    val views: String,
    val dateOfVideo: String,
    val duration: String,
    val channelName: String,
    val channelThumbnailsUrl: String
)

data class SavedVideosListData(
    val title: String,
    val watchUrl: String,
    val thumbnailUrl: String,
    val viewer: String,
    val dateTime: String,
    val duration: String,
    val channelName: String
)

data class ItemsStreamUrlsForMediaItemData(
    val audioUrl: String,
    val videoId: String,
    val title: String,
    val views: String,
    val dateOfVideo: String,
    val duration: String,
    val channelName: String,
    val channelThumbnailsUrl: String
)