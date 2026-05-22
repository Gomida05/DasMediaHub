package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

/**
 * Data class representing the root of a YouTube search response.
 *
 * @property result List of [Video] objects found for the query.
 */
@Serializable
data class SearchResponse(
    val result: List<Video>
)
