package com.das.mediaHub.ui.downloads

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.model.download.DownloadStatus
import com.das.mediaHub.data.model.download.DownloadingUiState
import com.das.mediaHub.data.downloader.DownloadQueueManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadingViewModel(application: Application) : AndroidViewModel(application) {

    private val queueManager = DownloadQueueManager.get(application)

    private val _uiState = MutableStateFlow<DownloadingUiState>(DownloadingUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                queueManager.restore()
            }.onFailure {
                _uiState.value = DownloadingUiState.Error(
                    it.message ?: "Failed to load downloads"
                )
            }
        }

        viewModelScope.launch {
            queueManager.states.collectLatest { states ->
                val downloads = states
                    .map { it.toDownloadInfo() }
                    .sortedWith(
                        compareBy {
                            when (it.status) {
                                DownloadStatus.DOWNLOADING -> 0
                                DownloadStatus.QUEUED -> 1
                                DownloadStatus.PAUSED -> 2
                                DownloadStatus.FAILED -> 3
                                DownloadStatus.COMPLETED -> 4
                                DownloadStatus.CANCELED -> 5
                            }
                        }
                    )

                _uiState.value = if (downloads.isEmpty()) {
                    DownloadingUiState.Empty
                } else {
                    DownloadingUiState.Success(downloads)
                }
            }
        }
    }

    fun pauseDownload(downloadId: String) {
        queueManager.pause(downloadId)
    }

    fun resumeDownload(downloadId: String) {
        queueManager.resume(downloadId)
    }

    fun cancelDownload(downloadId: String) {
        queueManager.cancel(downloadId)
    }

    fun removeFinished(downloadId: String) {
        queueManager.removeFinished(downloadId)
    }
}