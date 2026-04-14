package com.das.mediaHub.data.model.state

import com.das.downloader.data.model.AppUpdateInfo
import com.das.mediaHub.ui.theme.AppTheme

internal data class SettingsUiState(
    val appearanceExpanded: Boolean = false,
    val storageExpanded: Boolean = false,
    val showStorageDialog: Boolean = false,
    val versionName: String = "",
    val versionCode: Long = 0L,
    val updateState: UiState<AppUpdateInfo> = UiState.Idle
)