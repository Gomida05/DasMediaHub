package com.das.downloader.data.downloader

import com.das.downloader.data.model.AppUpdateInfo
import com.das.python.YouTuber.getAudioStreamUrl
import com.das.python.YouTuber.getVideoStreamUrl
import com.das.python.exceptions.PyCallError

/**
 * Coordinator responsible for managing high-level download operations.
 * 
 * It acts as an intermediary between the UI/Python layers and the [DownloaderRepo],
 * handling stream URL extraction for YouTube and delegating the actual 
 * queuing to the repository.
 * 
 * Example usage:
 * ```kotlin
 * val coordinator = DownloadCoordinator(downloaderRepo)
 * coordinator.enqueueVideoFromYoutube(
 *     videoId = "dQw4w9WgXcQ",
 *     title = "Rick Astley - Never Gonna Give You Up",
 *     onQueued = { id -> println("Download started: $id") },
 *     onError = { error -> println("Error: $error") }
 * )
 * ```
 */
class DownloadCoordinator(
    private val repo: DownloaderRepo
) {

    /**
     * Extracts the video stream URL for a YouTube video and enqueues it for download.
     * 
     * @param videoId The 11-character YouTube video ID.
     * @param title The title to be used for the downloaded file.
     * @param onQueued Callback triggered with the unique download task ID upon success.
     * @param onError Callback triggered with an error message if extraction or queuing fails.
     */
    suspend fun enqueueVideoFromYoutube(
        videoId: String,
        title: String,
        onQueued: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val loadStream = getVideoStreamUrl(videoId)
            val downloadId = repo.enqueueVideo(loadStream, title)
            onQueued(downloadId)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    /**
     * Extracts the audio stream URL for a YouTube video and enqueues it as a music download.
     * 
     * @param videoId The 11-character YouTube video ID.
     * @param title The title to be used for the downloaded file.
     * @param onQueued Callback triggered with the unique download task ID upon success.
     * @param onError Callback triggered with an error message.
     */
    suspend fun enqueueMusicFromYoutube(
        videoId: String,
        title: String,
        onQueued: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val loadStream = getAudioStreamUrl(mediaId = videoId)
            val downloadId = repo.enqueueMusic(loadStream, title)
            onQueued(downloadId)
        } catch (pyEx: PyCallError.PythonException) {
            onError(pyEx.message.toString())
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }

    /**
     * Enqueues a TikTok video for download using its URL.
     * 
     * @param url The full TikTok video URL.
     * @param title The title for the video.
     * @return The unique task ID for the download.
     */
    fun enqueueTiktokVideo(url: String, title: String): String {
        return repo.enqueueTiktokVideo(url = url, title = title)
    }

    /**
     * Enqueues an APK download, typically for app updates.
     * 
     * @param appInfo Information about the app update.
     * @return The unique task ID for the download.
     */
    fun downloadApk(appInfo: AppUpdateInfo): String {
        return repo.enqueueApk(appInfo)
    }

    /**
     * Pauses an ongoing download task.
     * @param taskId The ID of the task to pause.
     */
    fun pause(taskId: String) = repo.pause(taskId)

    /**
     * Resumes a paused download task.
     * @param taskId The ID of the task to resume.
     */
    fun resume(taskId: String) = repo.resume(taskId)

    /**
     * Cancels a download task and removes its progress.
     * @param taskId The ID of the task to cancel.
     */
    fun cancel(taskId: String) = repo.cancel(taskId)
}
