package com.das.mediaHub.ui.settings

import com.das.downloader.data.model.AppUpdateInfo
import com.das.downloader.data.model.PathType

sealed interface SettingsEffect {
    data class ShowMessage(val message: String) : SettingsEffect
    data class LaunchFolderPicker(val pathType: PathType) : SettingsEffect
    data class StartApkDownload(val appInfo: AppUpdateInfo) : SettingsEffect
}