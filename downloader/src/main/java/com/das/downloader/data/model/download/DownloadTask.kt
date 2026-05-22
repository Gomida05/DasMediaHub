package com.das.downloader.data.model.download

/**
 * Data class representing the static configuration for a new download request.
 * 
 * Unlike [DownloadState], this class holds the immutable parameters required 
 * to start a download.
 * 
 * @property id Unique identifier for the task.
 * @property url The remote source URL.
 * @property title Filename or title.
 * @property type Category of the download.
 * @property destinationPath Local destination path.
 * @property headers Optional HTTP headers for the request.
 * @property playlistName Optional playlist grouping.
 */
data class DownloadTask(
    val id: String,
    val url: String,
    val title: String,
    val type: DownloadType,
    val destinationPath: String,
    val headers: Map<String, String> = emptyMap(),
    val playlistName: String? = null
)
