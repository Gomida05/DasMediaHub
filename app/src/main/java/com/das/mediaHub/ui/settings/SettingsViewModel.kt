package com.das.mediaHub.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.downloader.AppUpdateRepository
import com.das.downloader.data.model.AppUpdateInfo
import com.das.mediaHub.BuildConfig
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.state.SettingsUiState
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.services.download.DownloadService
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
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appUpdateRepo: AppUpdateRepository
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
        _uiState.update {
            it.copy(
                updateState = UiState.Loading
            )
        }
        val currentCode = _uiState.value.versionCode.toInt()

        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            runCatching { appUpdateRepo.checkForUpdates() }
                .onSuccess { info ->
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
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            updateState = UiState.Error(ErrorMapper.map(throwable))
                        )
                    }
                }
        }
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

    fun onDownloadUpdateClicked(appInfo: AppUpdateInfo) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updateState = UiState.Idle
                )
            }
            DownloadService.startForApk(context, appInfo)
        }
    }
}