package com.das.mediaHub.ui.tiktok

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.model.tiktok.TikTokInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TikTokViewModel : ViewModel() {
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
        // Clear previous results when URL changes
        _resolvedInfo.value = null
        _error.value = null
    }

    fun fetchInfo() {
        val currentUrl = _url.value
        if (currentUrl.isBlank()) {
            _error.value = "Please enter a valid TikTok URL"
            return
        }

        _isLoading.value = true
        _error.value = null
        _resolvedInfo.value = null
        
        viewModelScope.launch {
            try {
                val tiktokInfo = TikTokUrlResolver.resolveTikTokVideoUrl(currentUrl)
                if (tiktokInfo.success && tiktokInfo.result != null) {
                    _resolvedInfo.value = tiktokInfo.result
                } else {
                    _error.value = "Beta 3.0 Error: Could not resolve video metadata"
                }
            } catch (e: Exception) {
                _error.value = "Beta 3.0 Error: ${e.localizedMessage}"
                println("Beta 3.0 Error: $e")
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
