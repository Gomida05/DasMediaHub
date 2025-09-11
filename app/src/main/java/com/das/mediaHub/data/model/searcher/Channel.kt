package com.das.mediaHub.data.model.searcher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Channel(
    val name: String,
    val id: String,
    val thumbnails: List<Thumbnail>? = null,
    val link: String
): Parcelable
