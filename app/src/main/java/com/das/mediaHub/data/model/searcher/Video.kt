package com.das.mediaHub.data.model.searcher


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Video(
    val type: String,
    val id: String,
    val title: String,
    val publishedTime: String? = null,
    val duration: String? = null,
    val viewCount: ViewCount? = null,
    val thumbnails: List<Thumbnail>? = null,
    val descriptionSnippet: List<DescriptionSnippet>? = null,
    val channel: Channel? = null,
    val accessibility: Accessibility? = null,
    val link: String,
    val shelfTitle: String? = null
): Parcelable