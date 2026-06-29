package com.das.mediaHub.ui.settings.download

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.downloader.data.local.DownloadPreferences
import com.das.downloader.data.model.PathType
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.repository.StorageRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Download Settings screen.
 */
data class DownloadSettingUiState(
    val downloadOverData: Boolean = false,
    val maxConcurrentDownloads: Int = 3,
)

/**
 * Effects for the Download Settings screen.
 */
sealed interface DownloadSettingEffect {
    /** Request to pick a folder for a specific [PathType]. */
    data class PickFolder(val pathType: PathType) : DownloadSettingEffect
    /** Show a snackbar message. */
    data class ShowMessage(val message: String) : DownloadSettingEffect
}

/**
 * ViewModel for the Download Settings screen, handling user preferences for downloads.
 */
@HiltViewModel
class DownloadSettingViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val storageRepo: StorageRepo
): ViewModel() {

    private val _effects = MutableSharedFlow<DownloadSettingEffect>()
    val effects = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(DownloadSettingUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.update {
            it.copy(
                downloadOverData = DownloadPreferences.getDownloadOverMobileData(context),
                maxConcurrentDownloads = DownloadPreferences.getMaxConcurrentDownloads(context)
            )
        }
    }

    // --- New Setting Updaters ---

    /**
     * Updates and persists the setting for downloading over mobile data.
     */
    fun updateDownloadOverData(enabled: Boolean) {
        DownloadPreferences.updateDownloadOverMobileData(context, enabled)
        _uiState.update { it.copy(downloadOverData = enabled) }
    }

    /**
     * Updates and persists the maximum number of concurrent downloads allowed.
     */
    fun updateMaxConcurrentDownloads(max: Int) {
        DownloadPreferences.updateMaxConcurrentDownloads(context, max)
        _uiState.update { it.copy(maxConcurrentDownloads = max) }
    }

    // --- Existing Storage Logic ---

    /**
     * Triggers a request for the user to pick a folder.
     */
    fun onPickFolderRequested(pathType: PathType) {
        viewModelScope.launch {
            _effects.emit(DownloadSettingEffect.PickFolder(pathType))
        }
    }

    /**
     * Handles the result of a folder picker request.
     */
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
                _effects.emit(DownloadSettingEffect.ShowMessage(ErrorMapper.map(e)))
            }
        }
    }
}
