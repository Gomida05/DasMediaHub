package com.das.mediaHub.ui.tiktok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.error.ErrorMapper
import com.das.mediaHub.data.model.tiktok.TikTokInfo
import com.das.mediaHub.data.model.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TikTokViewModel : ViewModel() {
    private val _url = MutableStateFlow("")
    val url = _url.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<TikTokInfo>>(UiState.Idle)
    val uiState = _uiState.asStateFlow()


    fun setUrl(newUrl: String) {
        _url.value = newUrl
        // Clear previous results when URL changes
        _uiState.value = UiState.Idle
    }

    fun fetchInfo() {
        val currentUrl = _url.value
        if (currentUrl.isBlank()) {
            _uiState.value = UiState.Error("Please enter a valid TikTok URL")
            return
        }

        _uiState.value = UiState.Loading
        
        viewModelScope.launch {
            try {
                val tiktokInfo = TikTokUrlResolver.resolveTikTokVideoUrl(currentUrl)
                val tiktokResult = tiktokInfo.result
                if (tiktokInfo.success && tiktokResult != null) {
                    _uiState.value = UiState.Success(tiktokResult)
                } else {
                    _uiState.value = UiState.Error(ErrorMapper.mapMessage(tiktokInfo.error))
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
