package com.das.forui.objectsAndData

import android.net.Uri


data class DownloadedListData(
    val title: String,
    val pathOfVideo: Uri,
    val thumbnailUri: Uri,
    val dateTime: String,
    val fileSize: String,
    val type: Int
)

data class VideoDetails(
    val title: String,
    val description: String,
    val viewNumber: String,
    val date: String,
    val channelName: String
)

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


data class SearchResultFromMain(
    val videoId: String,
    val title: String,
    val views: String,
    val dateOfVideo: String,
    val duration: String,
    val channelName: String,
    val channelThumbnailsUrl: String
)