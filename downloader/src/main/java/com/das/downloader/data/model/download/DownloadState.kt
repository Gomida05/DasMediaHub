package com.das.downloader.data.model.download

data class DownloadState(
    val id: String,
    val url: String,
    val title: String,
    val type: DownloadType,
    val destinationPath: String,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = -1L,
    val errorMessage: String? = null,
    val playlistName: String? = null
) {
    companion object {
        fun DownloadTask.toDownloadState(): DownloadState {
            return DownloadState(
                id = id,
                url = url,
                title = title,
                type = type,
                destinationPath = destinationPath,
                status = DownloadStatus.QUEUED,
                playlistName = playlistName
            )
        }
    }
}