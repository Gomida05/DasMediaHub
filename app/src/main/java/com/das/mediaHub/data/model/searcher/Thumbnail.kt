package com.das.mediaHub.data.model.searcher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
): Parcelable