package com.das.mediaHub.data.model.responds

import com.das.mediaHub.data.model.searcher.SearchResponse
import kotlinx.serialization.Serializable

@Serializable
data class ResponseVideo(
    val success: Boolean,
    val error: String?,
    val result: SearchResponse?
)