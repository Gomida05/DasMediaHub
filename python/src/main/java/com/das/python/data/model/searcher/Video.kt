package com.das.python.data.model.searcher


import kotlinx.serialization.Serializable

@Serializable
data class Video(
    val type: String? = null,
    val id: String,
    val title: String? = null,
    val publishedTime: String? = null,
    val duration: String? = null,
    val viewCount: ViewCount? = null,
    val thumbnails: List<Thumbnail>? = null,
    val descriptionSnippet: List<DescriptionSnippet>? = null,
    val channel: Channel? = null,
    val accessibility: Accessibility? = null,
    val link: String? = null,
    val shelfTitle: String? = null
)