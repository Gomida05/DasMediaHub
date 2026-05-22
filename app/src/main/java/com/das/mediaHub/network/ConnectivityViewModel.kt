package com.das.mediaHub.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    connectivityObserver: ConnectivityObserver
): ViewModel() {
    val networkState = connectivityObserver.getNetworkState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = NetworkDataClass.INITIALIZING
        )

    val isInitializing: Boolean
        get() = networkState.value == NetworkDataClass.INITIALIZING

}