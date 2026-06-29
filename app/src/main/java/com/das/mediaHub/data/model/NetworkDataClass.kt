package com.das.mediaHub.data.model

import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities

data class NetworkDataClass(
    val isConnected: Boolean,
    val error: String?,
    val network: Network?,
    val networkCapabilities: NetworkCapabilities?,
    val linkProperties: LinkProperties?
) {
    companion object {
        val INITIALIZING: NetworkDataClass
            get() = NetworkDataClass(
                isConnected = true,
                error = "Initializing",
                network = null,
                networkCapabilities = null,
                linkProperties = null
            )
    }
}