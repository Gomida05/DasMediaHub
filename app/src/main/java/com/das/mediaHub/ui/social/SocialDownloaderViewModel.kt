package com.das.mediaHub.ui.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.interfaces.UiState
import com.das.mediaHub.ui.components.dialogs.isValidSocialUrl
import com.das.python.SocialMediaClient
import com.das.python.data.model.media.MediaInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialDownloaderViewModel : ViewModel() {
    private val _url = MutableStateFlow("")
    val url = _url.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<MediaInfo>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()


    fun setUrl(newUrl: String) {
        _url.value = newUrl
        _uiState.value = UiState.Idle
    }

    fun fetchInfo() {
        val currentUrl = _url.value
        
        if (currentUrl.isBlank()) {
            _uiState.value = UiState.Error("Please enter a URL first.")
            return
        }

        if (!currentUrl.startsWith("http")) {
            _uiState.value = UiState.Error("Invalid link format. Make sure it starts with http:// or https://")
            return
        }

        if (!currentUrl.isValidSocialUrl()) {
            _uiState.value = UiState.Error("Unsupported platform. Please enter a valid TikTok or Instagram link.")
            return
        }

        _uiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                // Using getInstagramDetails for both platforms as the underlying logic is shared
                val response = SocialMediaClient.getUrlInfo(currentUrl)
                val result = response.result
                if (response.success && result != null) {
                    _uiState.value = UiState.Success(result)
                } else {
                    val rawError = response.error ?: "The content could not be retrieved. It might be private, deleted, or the link is invalid."
                    _uiState.value = UiState.Error(ErrorMapper.mapMessage(rawError))
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(ErrorMapper.map(e))
            }
        }
    }


    fun clearAll() {
        _url.value = ""
        _uiState.value = UiState.Idle
    }
}
