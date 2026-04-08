package com.das.downloader.data.model.download

enum class DownloadType(val extension: String) {
    VIDEO(".mp4"),
    MUSIC(".mp3"),
    TIKTOK_VIDEO("tiktok"),
    APK(".apk")
}