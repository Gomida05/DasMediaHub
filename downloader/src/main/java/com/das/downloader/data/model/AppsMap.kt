package com.das.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppsMap(
    @SerialName("DasMediaHub")
    val dasMediaHub: AppUpdateInfo? = null
)