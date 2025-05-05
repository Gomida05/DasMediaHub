package com.das.forui.objectsAndData

import androidx.compose.ui.graphics.vector.ImageVector


data object ForUIDataClass {


    data class PlayListDataClass(
        val url: String,
        val title: String,
        val views: String,
        val date: String,
        val duration: String

    )
    data class DownloadData(
        val title: String,
        val url: String
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
        val channelName: String,
        val channelThumbnail: String
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


    data class MyBottomNavData(
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector
    )
}
