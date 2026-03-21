package com.das.python.data.model.responds

import com.das.python.data.model.searcher.SearchResponse
import kotlinx.serialization.Serializable

@Serializable
data class ResponseVideo(
    val success: Boolean,
    val error: String?,
    val result: SearchResponse?
)