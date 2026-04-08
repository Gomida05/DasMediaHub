package com.das.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateInfo(
    @SerialName("latestVersionCode") val versionCode: Int,
    @SerialName("latestVersionName") val versionName: String,
    @SerialName("apkUrl") val appURL: String,
    @SerialName("changelog") val whatsNew: String
) {
    companion object {
        val EMPTY = AppUpdateInfo(-1, "", "", "")
    }

    fun isEmpty() = versionCode == -1 && appURL.isEmpty()
}