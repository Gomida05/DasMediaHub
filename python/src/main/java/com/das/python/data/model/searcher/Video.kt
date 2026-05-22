package com.das.python.data.model.searcher


import kotlinx.serialization.Serializable

/**
 * Data class representing a single video entry in a search result.
 *
 * @property type Type of result (e.g., "video").
 * @property id Unique video identifier.
 * @property title Video title.
 * @property publishedTime Relative time since upload.
 * @property duration Formatted video duration.
 * @property viewCount Detailed view count information.
 * @property thumbnails List of available thumbnail URLs and sizes.
 * @property descriptionSnippet Short snippet of the video description.
 * @property channel Metadata for the uploading channel.
 * @property accessibility Accessibility descriptions for the video.
 * @property link Full YouTube link to the video.
 * @property shelfTitle Title of the "shelf" category if part of a grouped result.
 */
@Serializable
data class Video(
    val type: String? = null,
    val id: String,
    val title: String? = null,
    val publishedTime: String? = null,
    val duration: String? = null,
    val viewCount: ViewCount? = null,
    val thumbnails: List<Thumbnail>? = null,
    val descriptionSnippet: List<DescriptionSnippet>? = null,
    val channel: Channel? = null,
    val accessibility: Accessibility? = null,
    val link: String? = null,
    val shelfTitle: String? = null
)
