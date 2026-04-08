package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String? = null,
    val id: String? = null,
    val thumbnails: List<Thumbnail>? = null,
    val link: String? = null
)