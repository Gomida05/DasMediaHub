package com.das.mediaHub.data.model.searcher

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val result: List<Video>
)
