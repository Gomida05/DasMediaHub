package com.das.python.data.model.media

import kotlinx.serialization.Serializable


@Serializable
data class StreamInfo(
    val url: String? = null,
    val ext: String? = null,
    val vcodec: String? = null,
    val acodec: String? = null,
    val format: String? = null,
    val height: Int? = null,
    val width: Int? = null
)