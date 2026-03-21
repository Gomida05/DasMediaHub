package com.das.python.data.model.responds

import com.das.python.data.model.FewVideoDetails
import kotlinx.serialization.Serializable


@Serializable
data class RespondVideoDetails(
    val success: Boolean,
    val error: String?,
    val result: FewVideoDetails?
)