package com.das.python.data.model.responds

import com.das.python.data.model.searcher.SearchResponse
import kotlinx.serialization.Serializable

/**
 * Wrapper for YouTube search results.
 *
 * @property success True if the search was successful.
 * @property error Error message if the search failed.
 * @property result The search response containing the list of videos, if successful.
 */
@Serializable
data class ResponseVideo(
    val success: Boolean,
    val error: String?,
    val result: SearchResponse?
)
