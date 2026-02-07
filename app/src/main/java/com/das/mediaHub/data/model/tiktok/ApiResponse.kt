package com.das.mediaHub.data.model.tiktok

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val error: String? = null,
    val result: T? = null
)