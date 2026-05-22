package com.das.mediaHub.network

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkObserver @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : ConnectivityObserver {


    override val getNetworkState: Flow<NetworkDataClass>
        get() = callbackFlow {

            // Emit initial state instantly
            val currentNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)

            trySend(
                NetworkDataClass(
                    isConnected = capabilities?.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    ) == true,
                    error = null,
                    network = currentNetwork,
                    networkCapabilities = capabilities,
                    linkProperties = connectivityManager.getLinkProperties(currentNetwork)
                )
            )

            val callback = object : NetworkCallback() {

                override fun onAvailable(network: Network) {
                    trySend(
                        NetworkDataClass(
                            isConnected = true,
                            error = null,
                            network = network,
                            networkCapabilities = connectivityManager.getNetworkCapabilities(network),
                            linkProperties = connectivityManager.getLinkProperties(network)
                        )
                    )
                }

                override fun onLost(network: Network) {
                    trySend(
                        NetworkDataClass(
                            isConnected = false,
                            error = "Lost",
                            network = network,
                            networkCapabilities = null,
                            linkProperties = null
                        )
                    )
                }

                override fun onUnavailable() {
                    trySend(
                        NetworkDataClass(
                            isConnected = false,
                            error = "Unavailable",
                            network = null,
                            networkCapabilities = null,
                            linkProperties = null
                        )
                    )
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val isValid = networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )

                    trySend(
                        NetworkDataClass(
                            isConnected = isValid,
                            error = null,
                            network = network,
                            networkCapabilities = networkCapabilities,
                            linkProperties = connectivityManager.getLinkProperties(network)
                        )
                    )
                }
            }

            connectivityManager.registerDefaultNetworkCallback(callback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
            .distinctUntilChanged()
}