package com.das.mediaHub.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class VideoDetails(
    val title: String,
    val description: String,
    val viewNumber: String,
    val date: String,
    val channelName: String
): Parcelable
