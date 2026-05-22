package com.das.downloader.data.downloader

import com.das.downloader.data.model.Outcome
import com.das.downloader.data.model.download.DownloadTask

interface Downloader {
    suspend fun download(
        task: DownloadTask,
        alreadyDownloadedBytes: Long,
        isPaused: () -> Boolean,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): Outcome
}