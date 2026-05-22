package com.das.downloader.data.model.download

/**
 * Enum representing the current execution phase of a download task.
 */
enum class DownloadStatus {
    /** Task is waiting for the background worker to start processing it. */
    QUEUED,
    /** Data is actively being transferred from the server to local storage. */
    DOWNLOADING,
    /** Download was suspended by the user and can be resumed later. */
    PAUSED,
    /** File has been fully downloaded and saved. */
    COMPLETED,
    /** An error occurred during the download process. */
    FAILED,
    /** Task was aborted by the user and partial data may have been deleted. */
    CANCELED
}
