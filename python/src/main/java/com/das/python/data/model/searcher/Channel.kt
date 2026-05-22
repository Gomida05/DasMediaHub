package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

/**
 * Data class representing a YouTube channel within a search result.
 *
 * @property name Channel display name.
 * @property id Unique channel identifier.
 * @property thumbnails List of channel profile picture thumbnails.
 * @property link Full link to the channel's page.
 */
@Serializable
data class Channel(
    val name: String? = null,
    val id: String? = null,
    val thumbnails: List<Thumbnail>? = null,
    val link: String? = null
)
