package com.das.mediaHub.data.model.tiktok

import kotlinx.serialization.Serializable

@Serializable
data class ResponseTikTok(
    val result: TikTokInfo? = null,
)
