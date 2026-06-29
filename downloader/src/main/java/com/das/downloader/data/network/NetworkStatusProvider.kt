package com.das.downloader.data.network

/**
 * Interface for checking the current network connectivity status.
 * This allows the downloader module to remain decoupled from the Android framework.
 */
interface NetworkStatusProvider {
    /**
     * Checks if the device is currently connected to the internet.
     */
    fun isConnected(): Boolean

    /**
     * Checks if the device is currently connected via a metered network (e.g., mobile data).
     */
    fun isMetered(): Boolean
}
