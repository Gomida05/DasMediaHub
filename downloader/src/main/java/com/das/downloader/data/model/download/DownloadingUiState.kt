package com.das.downloader.data.model.download

/**
 * Sealed interface representing the state of the downloads screen UI.
 */
sealed interface DownloadingUiState {
    /** UI is currently loading the list of downloads. */
    data object Loading : DownloadingUiState
    
    /** No downloads were found in the queue. */
    data object Empty : DownloadingUiState
    
    /** An error occurred while loading download data. */
    data class Error(val message: String) : DownloadingUiState
    
    /**
     * Successfully loaded the list of downloads.
     * 
     * @property downloads The list of all known download tasks.
     * @property hasActiveDownloads True if any task is currently active or pending.
     */
    data class Success(
        val downloads: List<DownloadInfo>,
        val hasActiveDownloads: Boolean = downloads.any {
            it.status == DownloadStatus.QUEUED ||
                    it.status == DownloadStatus.DOWNLOADING ||
                    it.status == DownloadStatus.PAUSED
        }
    ) : DownloadingUiState
}
