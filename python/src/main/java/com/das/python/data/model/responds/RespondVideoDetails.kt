package com.das.python.data.model.responds

import com.das.python.data.model.FewVideoDetails
import kotlinx.serialization.Serializable

/**
 * Wrapper for detailed video metadata responses.
 *
 * @property success True if the details were successfully retrieved.
 * @property error Error message if the retrieval failed.
 * @property result Detailed video information if successful.
 */
@Serializable
data class RespondVideoDetails(
    val success: Boolean,
    val error: String?,
    val result: FewVideoDetails?
)
