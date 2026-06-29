package com.das.downloader.data.downloader

/**
 * Represents the outcome of a download enqueue operation.
 *
 * This sealed class is used by the coordinator to communicate whether a
 * [DownloadRequest] was successfully translated into an active download task,
 * or if it failed during the preparation phase (e.g., failed to extract a stream URL).
 */
sealed class DownloadResult {

    /**
     * Indicates that the download request was successfully processed and added
     * to the underlying queue.
     *
     * @property taskId The unique identifier assigned to the active download task,
     * which can be used later to pause, resume, or cancel the download.
     */
    data class Success(val taskId: String) : DownloadResult()

    /**
     * Indicates that the download request failed to enqueue.
     *
     * @property exception The underlying exception that caused the failure
     * (e.g., network error, unsupported URL, or Python execution failure).
     */
    data class Error(val exception: Throwable) : DownloadResult()
}