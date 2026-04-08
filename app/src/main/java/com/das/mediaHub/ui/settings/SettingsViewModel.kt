package com.das.mediaHub.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.constants.UrlLists.APP_URL
import com.das.mediaHub.data.error.ErrorMapper
import com.das.downloader.data.model.AppUpdateInfo
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SettingsViewModel: ViewModel() {


    private val _apkInfoState = MutableStateFlow<UiState<AppUpdateInfo>>(UiState.Idle)
    val apkInfoState = _apkInfoState.asStateFlow()

    private var loadingJob: Job? = null


    fun loadJson() {
        _apkInfoState.value = UiState.Loading

        loadingJob = viewModelScope.launch {
            try {
                val result = requestJson()
                _apkInfoState.value = UiState.Success(result)
            } catch (e: Exception) {
                _apkInfoState.value = UiState.Error(ErrorMapper.map(e))
            }
        }
    }

    fun cancelLoading() {
        loadingJob?.cancel()
        clearResult()
    }


    fun retryLoad() {
        clearResult()
        loadJson()
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

    fun clearResult() {
        _apkInfoState.value = UiState.Idle
    }

}