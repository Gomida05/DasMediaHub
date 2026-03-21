package com.das.mediaHub.downloader

import android.content.Context
import com.das.mediaHub.data.model.AppUpdateInfo
import com.das.python.YouTuber.getAudioStreamUrl
import com.das.python.YouTuber.getVideoStreamUrl

class DownloadCoordinator(
    private val context: Context
) {
    private val repo = DownloaderRepo(context)

    suspend fun enqueueVideoFromYoutube(
        videoId: String,
        title: String,
        onQueued: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        getVideoStreamUrl(
            videoId,
            onSuccess = { url ->
                val id = repo.enqueueVideo(url, title)
                onQueued(id)
            },
            onFailure = { error ->
                onError(error ?: "Failed to get video URL")
            }
        )
    }

    suspend fun enqueueMusicFromYoutube(
        videoId: String,
        title: String,
        onQueued: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        getAudioStreamUrl(
            videoId,
            onSuccess = { url ->
                val id = repo.enqueueMusic(url, title)
                onQueued(id)
            },
            onFailure = { error ->
                onError(error ?: "Failed to get audio URL")
            }
        )
    }

    fun enqueueTiktokVideo(url: String, title: String): String {
        return repo.enqueueTiktokVideo(url = url, title = title)
    }

    fun downloadApk(appInfo: AppUpdateInfo): String {
        return repo.enqueueApk(appInfo)
    }


    fun pause(taskId: String) = repo.pause(taskId)
    fun resume(taskId: String) = repo.resume(taskId)
    fun cancel(taskId: String) = repo.cancel(taskId)
}