package com.das.downloader.data.downloader

import android.content.Context
import com.das.downloader.DownloadQueueManager
import com.das.downloader.data.local.DownloadPreferences
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
 *     appName = "DasMediaHub"
 * )
 * repo.enqueueVideo("https://example.com/video.mp4", "Sample Video")
 * ```
 */
class DownloaderRepo(
    private val queue: DownloadQueueManager,
    private val context: Context
) {

    /**
     * The resolved directory path for saving downloaded videos.
     * Retrieved dynamically based on the user's current settings in [DownloadPreferences].
     */
    private val videoPath: String
        get() = DownloadPreferences.getVideoPath(context)

    /**
     * The resolved directory path for saving downloaded audio files.
     * Retrieved dynamically based on the user's current settings in [DownloadPreferences].
     */
    private val audioPath: String
        get() = DownloadPreferences.getAudioPath(context)


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
                type = DownloadType.YOUTUBE_VIDEO,
                destinationPath = buildFilePath(base, title, DownloadType.YOUTUBE_VIDEO),
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
                type = DownloadType.YOUTUBE_AUDIO,
                destinationPath = buildFilePath(base, title, DownloadType.YOUTUBE_AUDIO),
                playlistName = playlistName
            )
        )
        return id
    }



    /**
     * Enqueues a TikTok or Instagram video for download.
     * 
     * @param url Direct video URL.
     * @param title Title for the video.
     * @return The unique task ID.
     */
    fun queueMediaDownload(url: String, title: String, mediaType: DownloadType): String {
        val id = DownloadQueueManager.newTaskId()
        val base = if (mediaType == DownloadType.YOUTUBE_AUDIO) audioPath else videoPath

        queue.enqueue(
            DownloadTask(
                id = id,
                url = url,
                title = title,
                type = mediaType,
                destinationPath = buildFilePath(base, title, mediaType)
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
                type = DownloadType.YOUTUBE_VIDEO,
                destinationPath = buildFilePath(base, title, DownloadType.YOUTUBE_VIDEO),
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
                type = DownloadType.YOUTUBE_AUDIO,
                destinationPath = buildFilePath(base, title, DownloadType.YOUTUBE_AUDIO),
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
