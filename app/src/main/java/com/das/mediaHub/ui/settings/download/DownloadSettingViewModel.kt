package com.das.mediaHub.ui.settings.download

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.downloader.data.model.PathType
import com.das.mediaHub.data.repository.StorageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 1. Define UI State for your new settings
data class DownloadSettingUiState(
    val downloadOverData: Boolean = false,
    val maxConcurrentDownloads: Int = 3,
    // Add other settings here as you expand
)

// 2. Safely type your effects so you can emit both folder requests and messages
sealed interface DownloadSettingEffect {
    data class PickFolder(val pathType: PathType) : DownloadSettingEffect
    data class ShowMessage(val message: String) : DownloadSettingEffect
}

@HiltViewModel
class DownloadSettingViewModel @Inject constructor(
    private val storageRepo: StorageRepo
    // private val pathPreferences: PathPreferences // Inject this too instead of calling a static method!
): ViewModel() {

    private val _effects = MutableSharedFlow<DownloadSettingEffect>()
    val effects = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(DownloadSettingUiState())
    val uiState = _uiState.asStateFlow()

    // --- New Setting Updaters ---

    fun updateDownloadOverData(enabled: Boolean) {
        // Here you would also save this to DataStore/SharedPreferences
        _uiState.value = _uiState.value.copy(downloadOverData = enabled)
    }

    fun updateMaxConcurrentDownloads(max: Int) {
        // Here you would also save this to DataStore/SharedPreferences
        _uiState.value = _uiState.value.copy(maxConcurrentDownloads = max)
    }

    // --- Existing Storage Logic ---

    fun onPickFolderRequested(pathType: PathType) {
        viewModelScope.launch {
            _effects.emit(DownloadSettingEffect.PickFolder(pathType))
        }
    }

    fun onFolderPicked(pathType: PathType, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            try {
                val savedPath = storageRepo.getFolderPathFromUri(uri)
                if (savedPath == null) {
                    _effects.emit(DownloadSettingEffect.ShowMessage("Could not use the selected folder"))
                    return@launch
                }

                storageRepo.persistFolderPermission(uri)

                storageRepo.updatePath(
                    pathType = pathType,
                    newPath = savedPath
                )

                _effects.emit(
                    DownloadSettingEffect.ShowMessage(
                        when (pathType) {
                            PathType.AUDIO -> "Audio folder updated"
                            PathType.VIDEO -> "Video folder updated"
                        }
                    )
                )
            } catch (e: Exception) {
                _effects.emit(DownloadSettingEffect.ShowMessage(e.message?:""))
            }
        }
    }


}