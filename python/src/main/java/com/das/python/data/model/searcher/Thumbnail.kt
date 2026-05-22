package com.das.python.data.model.searcher

import kotlinx.serialization.Serializable

/**
 * Data class representing a thumbnail image.
 *
 * @property url Direct link to the image.
 * @property width Image width in pixels.
 * @property height Image height in pixels.
 */
@Serializable
data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
)
