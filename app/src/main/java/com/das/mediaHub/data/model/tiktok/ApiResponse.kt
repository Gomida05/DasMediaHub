package com.das.mediaHub.data.model.tiktok

import kotlinx.serialization.Serializable

/**
 * Generic wrapper class for API responses.
 *
 * @param T The type of data returned in the [result] field.
 * @property success Whether the request was successful.
 * @property error Error message if the request failed.
 * @property result The payload of the response, if successful.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val error: String? = null,
    val result: T? = null
)
