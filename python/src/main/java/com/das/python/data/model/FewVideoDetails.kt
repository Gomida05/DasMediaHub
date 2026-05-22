package com.das.python.data.model

import kotlinx.serialization.Serializable

/**
 * Data class representing a subset of video details.
 *
 * @property title Video title.
 * @property description Video description snippet.
 * @property viewNumber Formatted number of views.
 * @property date Upload date.
 * @property channelName Name of the channel.
 */
@Serializable
data class FewVideoDetails(
    val title: String,
    val description: String,
    val viewNumber: String,
    val date: String,
    val channelName: String
)
