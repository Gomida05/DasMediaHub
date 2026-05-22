package com.das.mediaHub.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    val getNetworkState : Flow<NetworkDataClass>
}