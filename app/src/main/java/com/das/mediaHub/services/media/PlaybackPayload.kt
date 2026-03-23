package com.das.mediaHub.services.media

data class PlaybackPayload(
    val videoId: String,
    val mediaUrl: String,
    val title: String,
    val channelName: String,
    val views: String,
    val date: String,
    val duration: String
)