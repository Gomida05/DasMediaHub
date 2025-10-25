package com.das.mediaHub.data.model.responds

import android.os.Parcelable
import com.das.mediaHub.data.model.VideoDetails
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class RespondVideoDetails(
    val success: Boolean,
    val error: String?,
    val result: VideoDetails?
): Parcelable