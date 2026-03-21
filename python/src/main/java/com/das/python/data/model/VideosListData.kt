package com.das.python.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideosListData(
    val videoId: String,
    val title: String,
    val views: String,
    val dateOfVideo: String,
    val duration: String,
    val channelName: String,
    val channelThumbnailsUrl: String
)