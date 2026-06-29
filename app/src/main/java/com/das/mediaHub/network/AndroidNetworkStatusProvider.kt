package com.das.mediaHub.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.das.downloader.data.network.NetworkStatusProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [NetworkStatusProvider] using [ConnectivityManager].
 */
@Singleton
class AndroidNetworkStatusProvider @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : NetworkStatusProvider {

    override fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun isMetered(): Boolean {
        return connectivityManager.isActiveNetworkMetered
    }
}
