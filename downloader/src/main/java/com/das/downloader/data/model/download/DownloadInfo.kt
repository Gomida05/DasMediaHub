package com.das.downloader.data.model.download

/**
 * Data class containing a summary of a download's progress and status.
 * 
 * Used primarily for UI binding to show active download details.
 * 
 * @property id Unique identifier for the download task.
 * @property title The name of the file being downloaded.
 * @property progress Float value from 0.0 to 1.0 representing completion.
 * @property totalSize Total size of the file in bytes.
 * @property bytesDownloaded Number of bytes already saved to disk.
 * @property status Current [DownloadStatus] of the task.
 * @property errorMessage Descriptive error message if the task failed.
 * @property filePath Absolute local path where the file is being saved.
 */
data class DownloadInfo(
    val id: String,
    val title: String,
    val progress: Float,
    val totalSize: Long,
    val bytesDownloaded: Long,
    val status: DownloadStatus,
    val errorMessage: String? = null,
    val filePath: String = ""
)
