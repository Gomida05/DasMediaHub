package com.das.mediaHub.data.model.download

sealed interface DownloadingUiState {
    data object Loading : DownloadingUiState
    data object Empty : DownloadingUiState
    data class Error(val message: String) : DownloadingUiState
    data class Success(
        val downloads: List<DownloadInfo>,
        val hasActiveDownloads: Boolean = downloads.any {
            it.status == DownloadStatus.QUEUED ||
                    it.status == DownloadStatus.DOWNLOADING ||
                    it.status == DownloadStatus.PAUSED
        }
    ) : DownloadingUiState
}