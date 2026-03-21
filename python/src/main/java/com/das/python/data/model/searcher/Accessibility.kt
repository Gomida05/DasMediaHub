package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

@Serializable
data class Accessibility(
    val title: String? = null,
    val duration: String? = null
)