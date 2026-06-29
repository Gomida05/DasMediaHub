package com.das.mediaHub.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.mediaHub.data.local.UpdatePreferences
import com.das.mediaHub.data.model.PendingUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver,
    private val updatePreferences: UpdatePreferences
): ViewModel() {

    val networkState = connectivityObserver.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ConnectivityObserver.Status.Initializing
        )

    val pendingUpdate = updatePreferences.pendingUpdate
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PendingUpdate(
                apkPath = updatePreferences.getUpdateApkPath(),
                versionCode = updatePreferences.getUpdateVersionCode()
            )
        )

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog = _showUpdateDialog.asStateFlow()

    fun setShowUpdateDialog(show: Boolean) {
        _showUpdateDialog.value = show
    }


    fun clearPendingUpdate() {
        updatePreferences.clearPendingInstall()
    }

}