package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val result: List<Video>
)
