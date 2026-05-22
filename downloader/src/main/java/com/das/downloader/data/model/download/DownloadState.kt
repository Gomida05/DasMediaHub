package com.das.downloader.data.model.download

/**
 * Data class representing the full persistable state of a download task.
 * 
 * This object is stored and updated during the download lifecycle to ensure
 * that progress is maintained across application restarts.
 * 
 * @property id Unique identifier for the task.
 * @property url Remote URL of the file.
 * @property title Human-readable title.
 * @property type Category of the download (Video, Music, APK).
 * @property destinationPath Local absolute path where the file will be saved.
 * @property status Current execution status.
 * @property progress Percentage completion (0-100).
 * @property downloadedBytes Bytes saved so far.
 * @property totalBytes Total expected file size in bytes (-1 if unknown).
 * @property errorMessage Error detail if status is [DownloadStatus.FAILED].
 * @property playlistName Optional name of the grouping playlist.
 */
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
        /**
         * Converts a fresh [DownloadTask] into its initial [DownloadState].
         */
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
