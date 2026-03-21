package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val id: String,
    val thumbnails: List<Thumbnail>? = null,
    val link: String
)