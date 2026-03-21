package com.das.mediaHub.data.model.download

enum class DownloadType(val extension: String) {
    VIDEO(".mp4"),
    MUSIC(".mp3"),
    TIKTOK_VIDEO("tiktok"),
    APK(".apk")
}