package com.das.mediaHub.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViewerArgs(
    @SerialName("View_ID")
    val viewID: String,

    @SerialName("View_Title")
    val viewTitle: String? = null,

    @SerialName("duration")
    val duration: String? = null,

    @SerialName("View_Number")
    val viewNumber: String? = null,

    @SerialName("dateOfVideo")
    val dateOfVideo: String? = null,

    @SerialName("channelName")
    val channelName: String? = null,

    @SerialName("channel_Thumbnails")
    val channelThumbnails: String? = null
)