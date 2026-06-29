package com.das.downloader

import com.das.downloader.data.downloader.DownloadRequest
import com.das.downloader.data.downloader.DownloadResult
import com.das.downloader.data.downloader.DownloaderRepo
import com.das.downloader.data.model.download.DownloadType
import com.das.python.YouTuber
import com.das.python.exceptions.PyCallError

/**
 * Coordinator responsible for managing high-level download operations.
 *
 * It acts as an intermediary between the UI/Python layers and the [com.das.downloader.data.downloader.DownloaderRepo],
 * handling stream URL extraction for YouTube and delegating the actual
 * queuing to the repository.
 *
 * Example usage:
 * ```kotlin
 * val coordinator = DownloadCoordinator(downloaderRepo)
 * val request = DownloadRequest.YoutubeVideo(
 * videoId = "dQw4w9WgXcQ",
 * title = "Rick Astley - Never Gonna Give You Up"
 * )
 * * val result = coordinator.enqueue(request)
 * when (result) {
 * is DownloadResult.Success -> println("Download started with ID: ${result.id}")
 * is DownloadResult.Error -> println("Error occurred: ${result.exception.message}")
 * }
 * ```
 */
class DownloadCoordinator(
    private val repo: DownloaderRepo
) {

    /**
     * Processes a [DownloadRequest] by extracting necessary stream URLs (if applicable)
     * and enqueuing the task with the underlying repository.
     * * This method acts as a unified entry point for all download types. It resolves
     * actual media URLs (e.g., via [YouTuber] for YouTube requests) before passing
     * the finalized data to the [DownloaderRepo].
     *
     * @param request The specific [DownloadRequest] containing the required data
     * (e.g., video ID, direct URL, title, or app info).
     * @return A [DownloadResult.Success] containing the unique download task ID if successfully queued,
     * or a [DownloadResult.Error] containing the caught exception if extraction or queuing fails.
     */
    suspend fun enqueue(request: DownloadRequest): DownloadResult {
        return try {

            when (request) {

                is DownloadRequest.YoutubeVideo -> {
                    val url = YouTuber.getVideoStreamUrl(request.videoId)
                    val id = repo.enqueueVideo(url, request.title)
                    DownloadResult.Success(id)
                }

                is DownloadRequest.YoutubeAudio -> {
                    val url = YouTuber.getAudioStreamUrl(request.videoId)
                    val id = repo.enqueueMusic(url, request.title)
                    DownloadResult.Success(id)
                }

                is DownloadRequest.Social -> {
                    val id = repo.queueMediaDownload(
                        url = request.url,
                        title = request.title,
                        mediaType = request.downloadType
                    )
                    DownloadResult.Success(id)
                }
            }

        } catch (e: Exception) {
            DownloadResult.Error(e)
        }
    }


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
            val loadStream = YouTuber.getVideoStreamUrl(videoId)
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
            val loadStream = YouTuber.getAudioStreamUrl(mediaId = videoId)
            val downloadId = repo.enqueueMusic(loadStream, title)
            onQueued(downloadId)
        } catch (pyEx: PyCallError.PythonException) {
            onError(pyEx.message.toString())
        } catch (e: Exception) {
            onError(e.message.toString())
        }
    }

    /**
     * Enqueues a TikTok and  video for download using its URL.
     *
     * @param url The full TikTok or Instagram video URL.
     * @param title The title for the video.
     * @return The unique task ID for the download.
     */
    fun enqueueSocialMediaVideo(url: String, title: String, mediaType: DownloadType): String {
        return repo.queueMediaDownload(url = url, title = title, mediaType = mediaType)
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