package com.das.mediaHub.ui.settings

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SettingsViewModel: ViewModel() {

    private val _loading = mutableStateOf(false)
    val isLoading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val foundError: State<String?> = _error

    private val _apkInfo = mutableStateOf(AppUpdateInfo.EMPTY)
    val apkInfo: State<AppUpdateInfo> = _apkInfo

    private var loadingJob: Job? = null


    fun loadJson() {
        _loading.value = true
        _error.value = null

        loadingJob = viewModelScope.launch {
            try {
                val result = requestJson()
                _apkInfo.value = result
            } catch (e: Exception) {
                _error.value = "Found some error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun cancelLoading() {
        loadingJob?.cancel()
        _loading.value = false
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun retryLoad() {
        clearError()
        loadJson()
    }

    private suspend fun requestJson(): AppUpdateInfo = withContext(Dispatchers.IO) {
        val url = URL("https://github.com/Gomida05/Gomida05/raw/refs/heads/main/AppToDownload.json")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(response)
        val appsObject = jsonObject.getJSONObject("apps")
        val ytDownloader = appsObject.optJSONObject("DasMediaHub")
        val latestVersionCode = ytDownloader?.getInt("latestVersionCode")!!
        val latestVersionName = ytDownloader.getString("latestVersionName")
        val apkUrl = ytDownloader.getString("apkUrl")
        val changelog = ytDownloader.getString("changelog")

        AppUpdateInfo(
            latestVersionCode,
            latestVersionName,
            apkUrl,
            changelog
        )
    }

}