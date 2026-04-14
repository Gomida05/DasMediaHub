package com.das.mediaHub.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.core.content.pm.PackageInfoCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.downloader.data.local.PathPreferences.updatePath
import com.das.downloader.data.model.AppUpdateInfo
import com.das.downloader.data.model.PathType
import com.das.mediaHub.data.constants.UrlLists.APP_URL
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.state.SettingsUiState
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.ui.theme.AppTheme
import com.das.mediaHub.ui.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal class SettingsViewModel : ViewModel() {

    private var loadingJob: Job? = null
    private var initialized = false

    private val _effects = MutableSharedFlow<SettingsEffect>()
    val effects = _effects.asSharedFlow()

    private val _uiState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    fun initialize(packageInfo: PackageInfo) {
        if (initialized) return
        initialized = true

        _uiState.update {
            it.copy(
                versionName = packageInfo.versionName.orEmpty(),
                versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            )
        }

    }


    fun toggleAppearanceExpanded() {
        _uiState.update { it.copy(appearanceExpanded = !it.appearanceExpanded) }
    }

    fun showStorageDialog(show: Boolean) {
        _uiState.update { it.copy(showStorageDialog = show) }
    }

    fun onThemeSelected(context: Context, theme: AppTheme) {
        viewModelScope.launch {
            ThemePreferences.saveDarkMode(context, theme)
        }
    }

    fun onPickAudioFolderRequested() {
        viewModelScope.launch {
            showStorageDialog(false)
            _effects.emit(SettingsEffect.LaunchFolderPicker(PathType.AUDIO))
        }
    }

    fun onPickVideoFolderRequested() {
        viewModelScope.launch {
            showStorageDialog(false)
            _effects.emit(SettingsEffect.LaunchFolderPicker(PathType.VIDEO))
        }
    }

    fun onFolderPicked(context: Context, pathType: PathType, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            val savedPath = getFolderPathFromUri(context, uri)
            if (savedPath == null) {
                _effects.emit(SettingsEffect.ShowMessage("Could not use the selected folder"))
                return@launch
            }

            persistFolderPermission(context, uri)

            updatePath(
                context = context,
                pathType = pathType,
                newPath = savedPath
            )

            _effects.emit(
                SettingsEffect.ShowMessage(
                    when (pathType) {
                        PathType.AUDIO -> "Audio folder updated"
                        PathType.VIDEO -> "Video folder updated"
                    }
                )
            )
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
            runCatching { requestJson() }
                .onSuccess { info ->
                    if (info.versionCode <= currentCode) {
                        _uiState.update {
                            it.copy(
                                updateState = UiState.Idle
                            )
                        }
                        _effects.emit(SettingsEffect.ShowMessage("You're up to date"))
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
            _effects.emit(SettingsEffect.StartApkDownload(appInfo))
        }
    }

    private suspend fun requestJson(): AppUpdateInfo = withContext(Dispatchers.IO) {
        val url = URL(APP_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(response)
            val appsObject = jsonObject.optJSONObject("apps")
                ?: throw IllegalStateException("Missing 'apps' object in update response")

            val dasMediaHub = appsObject.optJSONObject("DasMediaHub")
                ?: throw IllegalStateException("Missing 'DasMediaHub' object in update response")

            val latestVersionCode = dasMediaHub.optInt("latestVersionCode", -1)
            val latestVersionName = dasMediaHub.optString("latestVersionName", "")
            val apkUrl = dasMediaHub.optString("apkUrl", "")
            val changelog = dasMediaHub.optString("changelog", "")

            if (latestVersionCode == -1 || apkUrl.isBlank()) {
                throw IllegalStateException("Invalid update information received")
            }

            AppUpdateInfo(
                versionCode = latestVersionCode,
                versionName = latestVersionName,
                appURL = apkUrl,
                whatsNew = changelog
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun persistFolderPermission(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
    }

    private fun getFolderPathFromUri(context: Context, uri: Uri): String? {
        return try {
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            if (documentFile != null && documentFile.isDirectory) {
                "/storage/emulated/0/${extractFolderPath(uri.path.toString())}"
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractFolderPath(path: String): String {
        return path.removePrefix("/tree/primary:")
    }
}