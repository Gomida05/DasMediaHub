package com.das.mediaHub.network

import com.das.mediaHub.data.model.NetworkDataClass
import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    enum class Status {
        Initializing,
        Available, Unavailable, Losing, Lost
    }

    val getNetworkState : Flow<NetworkDataClass>

    fun observe(): Flow<Status>


}