package com.das.mediaHub.ui.downloads

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.downloader.DownloadQueueManager
import com.das.downloader.data.model.download.DownloadStatus
import com.das.downloader.data.model.download.DownloadingUiState
import com.das.mediaHub.data.error.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadingViewModel @Inject constructor(
    private val queueManager: DownloadQueueManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DownloadingUiState>(DownloadingUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        restoreState()
        viewModelScope.launch {
            queueManager.states.collectLatest { states ->
                val downloads = states
                    .map { it.toDownloadInfo() }
                    .filter { 
                        it.status != DownloadStatus.COMPLETED && 
                        it.status != DownloadStatus.CANCELED 
                    }
                    .distinctBy { it.id }
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

    fun restoreState() {

        viewModelScope.launch {
            runCatching {
                queueManager.restore()
            }.onFailure {
                _uiState.value = DownloadingUiState.Error(
                    ErrorMapper.map(it)
                )
                Log.d(
                    "Downloading ViewModel",
                    it.message ?: "Failed to load downloads",
                    it
                )
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