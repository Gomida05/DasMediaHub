package com.das.mediaHub.python.data

import kotlinx.serialization.Serializable

@Serializable
data class StreamUrlRespond(
    val success: Boolean,
    val error: String?,
    val result: String?
)