package com.das.downloader.data.repository

import com.das.downloader.data.downloader.DownloadRequest
import com.das.downloader.data.model.download.DownloadTask

/**
 * Repository responsible for resolving download requests into actionable download tasks.
 *
 * It handles the extraction of direct stream URLs (e.g., from YouTube) and generates
 * safe, unique file paths on the device's storage. If a file with the same name
 * already exists, it automatically appends a numeric suffix (e.g., "(1)").
 *
 * Example usage:
 * ```kotlin
 * val repository: MediaDownloadRepository = MediaDownloadRepositoryImpl(context)
 * val request = DownloadRequest.YoutubeVideo(videoId = "...", title = "My Video")
 * val task = repository.resolveTask(request)
 * println("Download task ready: ${task.url} -> ${task.destinationPath}")
 * ```
 */
interface MediaDownloadRepository {

    /**
     * Resolves a [DownloadRequest] into a [DownloadTask] by extracting URLs and 
     * preparing the destination file path.
     *
     * @param request The request containing media metadata.
     * @return A [DownloadTask] ready for processing by a downloader.
     * @throws Exception if URL resolution fails.
     */
    suspend fun resolveTask(request: DownloadRequest): DownloadTask
}
