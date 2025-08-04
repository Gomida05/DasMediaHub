package com.das.mediaHub.data.model

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val appURL: String,
    val whatsNew: String
) {
    companion object {
        val EMPTY = AppUpdateInfo(
            versionCode = -1,
            versionName = "",
            appURL = "",
            whatsNew = ""
        )
    }

    fun isEmpty(): Boolean {
        return versionCode== -1 && appURL.isEmpty()
    }
}
