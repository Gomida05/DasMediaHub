package com.das.mediaHub.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoDetails(
    val title: String,
    val description: String,
    val viewNumber: String,
    val date: String,
    val channelName: String
)
