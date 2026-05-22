package com.das.downloader.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRootResponse(
    val apps: AppsMap? = null
)