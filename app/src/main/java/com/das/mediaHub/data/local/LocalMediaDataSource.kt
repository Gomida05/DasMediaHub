package com.das.mediaHub.data.local

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source responsible for retrieving metadata and file lists for local media assets.
 *
 * It handles both standard [File] based access and Android's `content://` URIs via 
 * [ContentResolver].
 *
 * Example usage:
 * ```kotlin
 * @Inject
 * lateinit var dataSource: LocalMediaDataSource
 * val metadata = dataSource.getMetadata(videoUri)
 * ```
 */
@Singleton
class LocalMediaDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val resolver = context.contentResolver

    /**
     * Resolves and returns the [MediaMetadata] for a given [Uri].
     * 
     * @param uri The URI of the local media file.
     * @return [MediaMetadata] containing the title and other available info.
     */
    fun getMetadata(uri: Uri): MediaMetadata {
        return if (uri.scheme == "content") {
            getFromContentUri(uri)
        } else {
            getFromFileUri(uri)
        }
    }

    /**
     * Scans a specific directory for media files and returns them as a list of [MediaItem]s.
     * 
     * @param currentMediaId The ID (URI) of the file to exclude from the results (e.g., current playing video).
     * @param pathLocation The absolute directory path to scan.
     * @return A list of [MediaItem] objects representing the found files.
     */
    fun scanFolder(
        currentMediaId: String,
        pathLocation: String
    ): List<MediaItem> {
        val dir = File(pathLocation)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()
            ?.filter { it.isFile && it.toUri().toString() != currentMediaId }
            ?.map {
                MediaItem.Builder()
                    .setMediaId(it.toUri().toString())
                    .setUri(it.toUri())
                    .build()
            }
            ?: emptyList()
    }

    /**
     * Internal helper to extract metadata from a content URI using [MediaStore].
     */
    private fun getFromContentUri(uri: Uri): MediaMetadata {
        var displayName: String? = null
        var title: String? = null

        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE
        )

        resolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)

                if (displayNameIndex != -1) {
                    displayName = cursor.getString(displayNameIndex)
                }

                if (titleIndex != -1) {
                    title = cursor.getString(titleIndex)
                }
            }
        }

        val finalTitle = title ?: displayName ?: uri.lastPathSegment ?: "Unknown video"

        return MediaMetadata.Builder()
            .setTitle(finalTitle)
            .setArtworkUri(uri)
            .setSubtitle(displayName)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }

    /**
     * Internal helper to extract metadata from a standard file system URI.
     */
    private fun getFromFileUri(uri: Uri): MediaMetadata {
        val file = File(uri.path.orEmpty())
        val name = file.name.ifBlank { uri.lastPathSegment ?: "Unknown video" }

        return MediaMetadata.Builder()
            .setTitle(name)
            .setSubtitle(file.parentFile?.name)
            .setMediaType(MediaMetadata.MEDIA_TYPE_VIDEO)
            .build()
    }
}
