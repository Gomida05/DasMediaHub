package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

/**
 * Data class representing view count information.
 *
 * @property text Full view count string (e.g., "1,234,567 views").
 * @property short Shortened view count string (e.g., "1.2M views").
 */
@Serializable
data class ViewCount(
    val text: String? = null,
    val short: String? = null
)
