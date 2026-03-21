package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

@Serializable
data class ViewCount(
    val text: String? = null,
    val short: String? = null
)