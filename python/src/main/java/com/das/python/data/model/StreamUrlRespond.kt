package com.das.python.data.model

import kotlinx.serialization.Serializable

/**
 * Wrapper class for responses containing a streaming URL.
 *
 * @property success True if the URL was successfully extracted.
 * @property error Error message if extraction failed, null otherwise.
 * @property result The direct streaming URL if successful, null otherwise.
 */
@Serializable
data class StreamUrlRespond(
    val success: Boolean,
    val error: String?,
    val result: String?
)
