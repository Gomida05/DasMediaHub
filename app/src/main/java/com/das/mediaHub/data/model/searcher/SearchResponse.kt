package com.das.mediaHub.data.model.searcher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SearchResponse(
    val result: List<Video>
): Parcelable
