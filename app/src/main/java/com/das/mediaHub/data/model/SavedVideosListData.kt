package com.das.mediaHub.data.model


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