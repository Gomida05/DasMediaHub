package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable


@Serializable
data class VideoDetails(
    val id: String,
    val title: String,
    val viewCount: ViewCount,
    val thumbnails: List<Thumbnail>,
    val description: String,
    val channel: ChannelDetails,
    val averageRating: Double? = null,
    val keywords: List<String>? = null,
    val publishDate: String? = null,
    val uploadDate: String? = null,
    val link: String
)