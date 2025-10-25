package com.das.mediaHub.python.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class StreamUrlRespond(
    val success: Boolean,
    val error: String?,
    val result: String?
): Parcelable