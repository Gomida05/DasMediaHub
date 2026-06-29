package com.das.mediaHub.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.das.downloader.AppUpdateRepository
import com.das.mediaHub.BuildConfig
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.local.UpdatePreferences
import com.das.mediaHub.data.model.state.SettingsUiState
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.services.download.DownloadAPK
import com.das.mediaHub.ui.theme.AppTheme
import com.das.mediaHub.ui.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appUpdateRepo: AppUpdateRepository,
    private val updatePreferences: UpdatePreferences,
    private val worker: WorkManager
) : ViewModel() {

    private var loadingJob: Job? = null

    private val _effects = MutableSharedFlow<String>()
    val effects = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE.toLong()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleAppearanceExpanded() {
        _uiState.update { it.copy(appearanceExpanded = !it.appearanceExpanded) }
    }


    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            ThemePreferences.saveDarkMode(context, theme)
        }
    }

    fun checkForUpdates() {
        val pendingPath = updatePreferences.getUpdateApkPath()
        val pendingVersion = updatePreferences.getUpdateVersionCode()

        if (pendingPath != null && File(pendingPath).exists() && pendingVersion > BuildConfig.VERSION_CODE) {
            _uiState.update {
                it.copy(
                    showPendingUpdateDialog = true,
                    pendingUpdatePath = pendingPath,
                    pendingUpdateVersionCode = pendingVersion
                )
            }
            return
        }

        performRemoteCheck()
    }

    private fun performRemoteCheck() {
        _uiState.update {
            it.copy(
                updateState = UiState.Loading
            )
        }
        val currentCode = _uiState.value.versionCode.toInt()


        loadingJob = viewModelScope.launch {
            try {
                val info = appUpdateRepo.checkForUpdates()
                if (info.latestVersionCode <= currentCode) {
                    _uiState.update {
                        it.copy(
                            updateState = UiState.Idle
                        )
                    }
                    _effects.emit("You're up to date")
                } else {
                    _uiState.update {
                        it.copy(
                            updateState = UiState.Success(info)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        updateState = UiState.Error(ErrorMapper.map(e))
                    )
                }
            }
        }
    }

    fun deletePendingUpdateAndCheckAgain() {
        val path = _uiState.value.pendingUpdatePath
        if (path != null) {
            File(path).delete()
        }
        updatePreferences.clearPendingInstall()
        _uiState.update {
            it.copy(
                showPendingUpdateDialog = false,
                pendingUpdatePath = null,
                pendingUpdateVersionCode = -1
            )
        }
        performRemoteCheck()
    }

    fun dismissPendingUpdateDialog() {
        _uiState.update { it.copy(showPendingUpdateDialog = false) }
    }

    fun retryLoad() {
        checkForUpdates()
    }

    fun cancelLoading() {
        loadingJob?.cancel()
                _uiState.update {
            it.copy(
                updateState = UiState.Idle
            )
        }
    }

    fun dismissUpdateDialog() {
                _uiState.update {
            it.copy(
                updateState = UiState.Idle
            )
        }
    }

    fun onDownloadUpdateClicked() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updateState = UiState.Idle
                )
            }

            val downloadWork = OneTimeWorkRequestBuilder<DownloadAPK>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            worker.enqueue(downloadWork)
        }
    }
}