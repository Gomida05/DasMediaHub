package com.das.mediaHub.data.model.searcher

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Accessibility(
    val title: String,
    val duration: String
): Parcelable