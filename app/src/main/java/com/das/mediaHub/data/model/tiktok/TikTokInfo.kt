package com.das.mediaHub.data.model.tiktok

import kotlinx.serialization.Serializable

@Serializable
data class TikTokInfo(
    val id: String?,
    val title: String?,
    val duration: Int?,
    val thumbnail: String?,
    val uploader: String?,
    val view_count: Long?,
    val like_count: Long?,
    val webpage_url: String?,
    val stream_url: String?
)
