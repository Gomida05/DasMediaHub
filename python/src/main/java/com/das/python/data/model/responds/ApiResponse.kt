package com.das.python.data.model.responds

import kotlinx.serialization.Serializable

/**
 * Generic wrapper class for API responses.
 *
 * @param TYPE The type of data returned in the [result] field.
 * @property success Whether the request was successful.
 * @property error Error message if the request failed.
 * @property result The payload of the response, if successful.
 */
@Serializable
data class ApiResponse<TYPE>(
    val success: Boolean,
    val error: String? = null,
    val result: TYPE? = null
)