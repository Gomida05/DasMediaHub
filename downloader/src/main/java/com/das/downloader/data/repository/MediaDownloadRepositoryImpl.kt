package com.das.downloader.data.repository

import android.content.Context
import com.das.downloader.data.downloader.DownloadRequest
import com.das.downloader.data.local.DownloadPreferences
import com.das.downloader.data.model.download.DownloadTask
import com.das.downloader.data.model.download.DownloadType
import com.das.python.YouTuber
import java.io.File
import java.util.UUID

/**
 * Implementation of [MediaDownloadRepository] that uses [YouTuber] for stream extraction
 * and [DownloadPreferences] for directory management.
 */
class MediaDownloadRepositoryImpl(
    private val context: Context
) : MediaDownloadRepository {

    override suspend fun resolveTask(request: DownloadRequest): DownloadTask {
        return when (request) {
            is DownloadRequest.YoutubeVideo -> {
                val url = YouTuber.getVideoStreamUrl(request.videoId)
                val destination = getUniqueFilePath(
                    baseDir = DownloadPreferences.getVideoPath(context),
                    title = request.title,
                    type = DownloadType.YOUTUBE_VIDEO
                )
                DownloadTask(
                    id = UUID.randomUUID().toString(),
                    url = url,
                    title = request.title,
                    type = DownloadType.YOUTUBE_VIDEO,
                    destinationPath = destination
                )
            }

            is DownloadRequest.YoutubeAudio -> {
                val url = YouTuber.getAudioStreamUrl(request.videoId)
                val destination = getUniqueFilePath(
                    baseDir = DownloadPreferences.getAudioPath(context),
                    title = request.title,
                    type = DownloadType.YOUTUBE_AUDIO
                )
                DownloadTask(
                    id = UUID.randomUUID().toString(),
                    url = url,
                    title = request.title,
                    type = DownloadType.YOUTUBE_AUDIO,
                    destinationPath = destination
                )
            }

            is DownloadRequest.Social -> {
                val destination = getUniqueFilePath(
                    baseDir = if (request.downloadType == DownloadType.YOUTUBE_AUDIO) {
                        DownloadPreferences.getAudioPath(context)
                    } else {
                        DownloadPreferences.getVideoPath(context)
                    },
                    title = request.title,
                    type = request.downloadType
                )
                DownloadTask(
                    id = UUID.randomUUID().toString(),
                    url = request.url,
                    title = request.title,
                    type = request.downloadType,
                    destinationPath = destination
                )
            }
        }
    }

    /**
     * Generates a unique file path by checking for existing files and appending
     * a numeric suffix if necessary.
     */
    private fun getUniqueFilePath(baseDir: String, title: String, type: DownloadType): String {
        val safeTitle = title.toSafeFileName()
        val extension = type.extension
        val baseFile = File(baseDir)
        if (!baseFile.exists()) baseFile.mkdirs()

        var file = File(baseDir, "$safeTitle$extension")
        var counter = 1

        while (file.exists()) {
            file = File(baseDir, "$safeTitle ($counter)$extension")
            counter++
        }

        return file.absolutePath
    }

    private fun String.toSafeFileName(): String {
        return replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
