package com.das.mediaHub.data.model.download


data class DownloadProgress(
    val id: String,
    val title: String,
    val url: String,
    val filePath: String,
    val progress: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = -1L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val errorMessage: String? = null
)