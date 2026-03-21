package com.das.mediaHub.data.model.download

data class DownloadInfo(
    val id: String,
    val title: String,
    val progress: Float,
    val totalSize: Long,
    val bytesDownloaded: Long,
    val status: DownloadStatus,
    val errorMessage: String? = null,
    val filePath: String = ""
)