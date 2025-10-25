package com.das.mediaHub.data.model.responds

import android.os.Parcelable
import com.das.mediaHub.data.model.searcher.SearchResponse
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ResponseVideo(
    val success: Boolean,
    val error: String?,
    val result: SearchResponse?
): Parcelable