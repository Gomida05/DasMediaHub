package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

@Serializable
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
)