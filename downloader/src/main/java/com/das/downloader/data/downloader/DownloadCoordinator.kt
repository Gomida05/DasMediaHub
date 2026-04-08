package com.das.downloader.data.downloader

import com.das.downloader.data.model.AppUpdateInfo
import com.das.python.YouTuber.getAudioStreamUrl
import com.das.python.YouTuber.getVideoStreamUrl
import com.das.python.exceptions.PyCallError

class DownloadCoordinator(
    private val repo: DownloaderRepo
) {

    suspend fun enqueueVideoFromYoutube(
        videoId: String,
        title: String,
        onQueued: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val loadStream = getVideoStreamUrl(videoId)

        try {
            val downloadId = repo.enqueueVideo(loadStream, title)
            onQueued(downloadId)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error")
        }
    }

    suspend fun enqueueMusicFromYoutube(
        videoId: String,
        title: String,
        onQueued: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val loadStream = getAudioStreamUrl(mediaId = videoId)
        try {
            val downloadId = repo.enqueueMusic(loadStream, title)
            onQueued(downloadId)
        } catch (pyEx: PyCallError.PythonException) {
            onError(pyEx.message.toString())
        } catch (e: Exception) {
            onError(e.message.toString())
        }
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