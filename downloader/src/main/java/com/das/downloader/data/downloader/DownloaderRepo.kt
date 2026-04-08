package com.das.downloader.data.downloader

import android.content.Context
import android.os.Environment
import com.das.downloader.data.local.PathPreferences.getAudioPath
import com.das.downloader.data.local.PathPreferences.getVideoPath
import com.das.downloader.data.model.AppUpdateInfo
import com.das.downloader.data.model.download.DownloadTask
import com.das.downloader.data.model.download.DownloadType
import java.io.File

class DownloaderRepo(
    val context: Context
) {
    private val queue = DownloadQueueManager.get(context)

    fun enqueueVideo(url: String, title: String, playlistName: String? = null): String {
        val base = if (playlistName.isNullOrBlank()) {
            getVideoPath(context)
        } else {
            File(getVideoPath(context), playlistName.toSafeFileName()).apply { mkdirs() }.absolutePath
        }

        val id = DownloadQueueManager.newTaskId()
        queue.enqueue(
            DownloadTask(
                id = id,
                url = url,
                title = title,
                type = DownloadType.VIDEO,
                destinationPath = buildFilePath(base, title, DownloadType.VIDEO),
                playlistName = playlistName
            )
        )
        return id
    }

    fun enqueueMusic(url: String, title: String, playlistName: String? = null): String {
        val base = if (playlistName.isNullOrBlank()) {
            getAudioPath(context)
        } else {
            File(getAudioPath(context), playlistName.toSafeFileName()).apply {
                mkdirs()
            }.absolutePath
        }

        val id = DownloadQueueManager.newTaskId()
        queue.enqueue(
            DownloadTask(
                id = id,
                url = url,
                title = title,
                type = DownloadType.MUSIC,
                destinationPath = buildFilePath(base, title, DownloadType.MUSIC),
                playlistName = playlistName
            )
        )
        return id
    }

    fun enqueueApk(appInfo: AppUpdateInfo): String {
        val base = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
            ?: context.filesDir.absolutePath

        val id = DownloadQueueManager.newTaskId()
        val title = context.applicationContext.applicationInfo.name
        queue.enqueue(
            DownloadTask(
                id = id,
                url = appInfo.appURL,
                title = title,
                type = DownloadType.APK,
                destinationPath = buildFilePath(base, title, DownloadType.APK)
            )
        )
        return id
    }

    fun enqueueTiktokVideo(url: String, title: String): String {
        val base = File(getVideoPath(context), title.toSafeFileName()).apply { mkdirs() }.absolutePath
        val id = DownloadQueueManager.newTaskId()
        queue.enqueue(
            DownloadTask(
                id = id,
                url = url,
                title = title,
                type = DownloadType.VIDEO,
                destinationPath = buildFilePath(base, title, DownloadType.VIDEO)
            )
        )
        return id
    }



    fun enqueuePlaylistVideos(items: List<Pair<String, String>>, playlistName: String) {
        val base = File(getVideoPath(context), playlistName.toSafeFileName()).apply { mkdirs() }.absolutePath
        val tasks = items.map { (url, title) ->
            DownloadTask(
                id = DownloadQueueManager.newTaskId(),
                url = url,
                title = title,
                type = DownloadType.VIDEO,
                destinationPath = buildFilePath(base, title, DownloadType.VIDEO),
                playlistName = playlistName
            )
        }
        DownloadQueueManager.get(context).enqueuePlaylist(tasks)
    }

    fun enqueuePlaylistMusic(items: List<Pair<String, String>>, playlistName: String) {
        val base = File(getAudioPath(context), playlistName.toSafeFileName()).apply { mkdirs() }.absolutePath
        val tasks = items.map { (url, title) ->
            DownloadTask(
                id = DownloadQueueManager.newTaskId(),
                url = url,
                title = title,
                type = DownloadType.MUSIC,
                destinationPath = buildFilePath(base, title, DownloadType.MUSIC),
                playlistName = playlistName
            )
        }
        DownloadQueueManager.get(context).enqueuePlaylist(tasks)
    }

    fun pause(id: String) = queue.pause(id)
    fun resume(id: String) = queue.resume(id)
    fun cancel(id: String) = queue.cancel(id)

    fun String.toSafeFileName(): String {
        return replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun buildFilePath(
        baseDir: String,
        title: String,
        type: DownloadType
    ): String {
        val safeName = title.toSafeFileName()
        return File(baseDir, "$safeName${type.extension}").absolutePath
    }
}