package com.das.downloader.data.model.download

data class DownloadTask(
    val id: String,
    val url: String,
    val title: String,
    val type: DownloadType,
    val destinationPath: String,
    val headers: Map<String, String> = emptyMap(),
    val playlistName: String? = null
)
