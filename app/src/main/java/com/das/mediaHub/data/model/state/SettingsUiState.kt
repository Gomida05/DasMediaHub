package com.das.mediaHub.data.model.state

import com.das.downloader.data.model.AppUpdateInfo

/**
 * Data class representing the state of the Settings screen.
 *
 * @property appearanceExpanded Whether the appearance section is expanded.
 * @property storageExpanded Whether the storage section is expanded.
 * @property versionName Current application version name.
 * @property versionCode Current application version code.
 * @property updateState Current state of the app update check.
 */
data class SettingsUiState(
    val appearanceExpanded: Boolean = false,
    val storageExpanded: Boolean = false,
    val versionName: String = "",
    val versionCode: Long = 0L,
    val updateState: UiState<AppUpdateInfo> = UiState.Idle
)
