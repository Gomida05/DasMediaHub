package com.das.downloader.data.downloader

import com.das.downloader.data.model.AppUpdateInfo
import com.das.downloader.data.model.download.DownloadTask
import com.das.downloader.data.model.download.DownloadType
import java.io.File

/**
 * Repository class that handles the persistence and queuing of download tasks.
 *
 * This version is designed with clean architecture principles in mind, being 
 * completely decoupled from the Android Framework context.
 * 
 * Example usage:
 * ```kotlin
 * val repo = DownloaderRepo(
 *     queue = downloadQueueManager,
 *     videoPath = "/sdcard/Movies/DasMediaHub",
 *     audioPath = "/sdcard/Music/DasMediaHub",
 *     apkPath = "/sdcard/Download",
 *     appName = "DasMediaHub"
 * )
 * repo.enqueueVideo("https://example.com/video.mp4", "Sample Video")
 * ```
 */
class DownloaderRepo(
    private val queue: DownloadQueueManager,
    private val videoPath: String,
    private val audioPath: String,
    private val apkPath: String,
    private val appName: String
) {

    /**
     * Enqueues a video for download.
     * 
     * @param url Direct URL to the video stream.
     * @param title Title of the video.
     * @param playlistName Optional name of the playlist to group the download.
     * @return The unique task ID.
     */
    fun enqueueVideo(url: String, title: String, playlistName: String? = null): String {
        val base = if (playlistName.isNullOrBlank()) {
            videoPath
        } else {
            File(videoPath, playlistName.toSafeFileName()).apply { mkdirs() }.absolutePath
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

    /**
     * Enqueues music for download.
     * 
     * @param url Direct URL to the audio stream.
     * @param title Title of the track.
     * @param playlistName Optional name of the playlist.
     * @return The unique task ID.
     */
    fun enqueueMusic(url: String, title: String, playlistName: String? = null): String {
        val base = if (playlistName.isNullOrBlank()) {
            audioPath
        } else {
            File(audioPath, playlistName.toSafeFileName()).apply { mkdirs() }.absolutePath
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

    /**
     * Enqueues an APK for download.
     * 
     * @param appInfo Information about the app update/APK.
     * @return The unique task ID.
     */
    fun enqueueApk(appInfo: AppUpdateInfo): String {
        val id = DownloadQueueManager.newTaskId()
        queue.enqueue(
            DownloadTask(
                id = id,
                url = appInfo.apkUrl,
                title = appName,
                type = DownloadType.APK,
                destinationPath = buildFilePath(apkPath, appName, DownloadType.APK)
            )
        )
        return id
    }

    /**
     * Enqueues a TikTok video for download.
     * 
     * @param url Direct video URL.
     * @param title Title for the video.
     * @return The unique task ID.
     */
    fun enqueueTiktokVideo(url: String, title: String): String {
        val base = File(videoPath, title.toSafeFileName()).apply { mkdirs() }.absolutePath
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

    /**
     * Enqueues multiple videos as part of a playlist.
     * 
     * @param items List of Pair(url, title).
     * @param playlistName Name of the playlist folder.
     */
    fun enqueuePlaylistVideos(items: List<Pair<String, String>>, playlistName: String) {
        val base = File(videoPath, playlistName.toSafeFileName()).apply { mkdirs() }.absolutePath
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
        queue.enqueuePlaylist(tasks)
    }

    /**
     * Enqueues multiple tracks as part of a music playlist.
     * 
     * @param items List of Pair(url, title).
     * @param playlistName Name of the playlist folder.
     */
    fun enqueuePlaylistMusic(items: List<Pair<String, String>>, playlistName: String) {
        val base = File(audioPath, playlistName.toSafeFileName()).apply { mkdirs() }.absolutePath
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
        queue.enqueuePlaylist(tasks)
    }

    /** Pauses a download task. */
    fun pause(id: String) = queue.pause(id)
    /** Resumes a download task. */
    fun resume(id: String) = queue.resume(id)
    /** Cancels a download task. */
    fun cancel(id: String) = queue.cancel(id)

    /**
     * Extension to clean a string for use as a safe filename.
     */
    private fun String.toSafeFileName(): String {
        return replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Constructs a full absolute file path for a download.
     * 
     * @param baseDir The directory to save the file in.
     * @param title The desired filename (without extension).
     * @param type The download type, used to determine the extension.
     * @return Full absolute path.
     */
    private fun buildFilePath(baseDir: String, title: String, type: DownloadType): String {
        val safeName = title.toSafeFileName()
        val ext = if (type.extension.startsWith(".")) type.extension else ".${type.extension}"
        return File(baseDir, "$safeName$ext").absolutePath
    }
}
