package com.das.mediaHub.data.constants

/**
 * Internal constants for identifying Intent extras and broadcast actions 
 * related to the download service.
 */
internal object DownloadConstants {
    /** Intent extra key for passing a specific download ID. */
    const val EXCEPTED_DOWNLOAD_ID = "com.das.mediaHub.DownloaderClass.EXCEPTED_DOWNLOAD_ID"
    
    /** Broadcast action string indicating that a download has finished. */
    const val DOWNLOAD_FINISHED = "com.das.mediaHub.DownloaderClass.DOWNLOAD_FINISHED"
}
