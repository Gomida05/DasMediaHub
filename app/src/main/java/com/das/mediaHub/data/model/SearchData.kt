package com.das.mediaHub.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a search query or a saved search history item.
 *
 * @property id Unique identifier for the search entry.
 * @property value The search query string.
 *
 * Example usage:
 * ```kotlin
 * val recentSearch = SearchData(id = "1", value = "lofi hip hop")
 * ```
 */
@Serializable
data class SearchData(
    val id: String,
    val value: String
)
