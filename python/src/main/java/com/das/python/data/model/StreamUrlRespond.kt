package com.das.python.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StreamUrlRespond(
    val success: Boolean,
    val error: String?,
    val result: String?
)