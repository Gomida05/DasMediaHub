package com.das.mediaHub.ui.instagram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.model.tiktok.TikTokInfo
import com.das.mediaHub.ui.tiktok.TikTokUrlResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InstagramViewModel : ViewModel() {
    private val _url = MutableStateFlow("")
    val url = _url.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _resolvedInfo = MutableStateFlow<TikTokInfo?>(null)
    val resolvedInfo = _resolvedInfo.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun setUrl(newUrl: String) {
        _url.value = newUrl
        _resolvedInfo.value = null
        _error.value = null
    }

    fun fetchInfo() {
        val currentUrl = _url.value
        if (currentUrl.isBlank()) {
            _error.value = "Please enter a valid Instagram URL"
            return
        }

        _isLoading.value = true
        _error.value = null
        _resolvedInfo.value = null
        
        viewModelScope.launch {
            try {
                // Reusing the TikTok logic as a placeholder for Instagram scraping
                val info = TikTokUrlResolver.resolveTikTokVideoUrl(currentUrl)
                if (info.success && info.error == null) {
                    _resolvedInfo.value = info.result
                } else {
                    _error.value = info.error
                }
            } catch (e: Exception) {
                _error.value = "Beta 1.0 Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAll() {
        _url.value = ""
        _resolvedInfo.value = null
        _error.value = null
    }
}
