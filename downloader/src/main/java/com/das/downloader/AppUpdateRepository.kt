package com.das.downloader

import com.das.downloader.data.model.AppUpdateInfo
import com.das.downloader.data.model.UpdateRootResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get

/**
 * Repository responsible for fetching and parsing application update information
 * from a remote JSON source.
 *
 * Example usage:
 * ```kotlin
 * val repository = AppUpdateRepository("https://example.com/update.json", httpClient)
 * try {
 *     val updateInfo = repository.checkForUpdates()
 *     println("Latest version: ${updateInfo.versionName}")
 * } catch (e: Exception) {
 *     println("Failed to check for updates: ${e.message}")
 * }
 * ```
 *
 * @property remoteUrl The URL where the update JSON file is hosted.
 * @property client The [HttpClient] used to perform the network request.
 */
class AppUpdateRepository(
    private val remoteUrl: String,
    private val client: HttpClient
) {

    /**
     * Checks for available updates by requesting the remote JSON data.
     *
     * @return [AppUpdateInfo] containing version codes, URLs, and changelogs.
     * @throws IllegalStateException if the response structure is invalid or missing required fields.
     */
    suspend fun checkForUpdates(): AppUpdateInfo = requestJson()

    /**
     * Internal helper to perform the GET request and deserialize the response.
     * Configures a 10-second timeout for the request.
     */
    private suspend fun requestJson(): AppUpdateInfo {
        val response = client.get(remoteUrl) {
            timeout {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 10_000
            }
        }.body<UpdateRootResponse>()

        val dasMediaHub = response.apps?.dasMediaHub
            ?: throw IllegalStateException("Missing 'apps' or 'DasMediaHub' object")

        if (dasMediaHub.latestVersionCode == -1 || dasMediaHub.apkUrl.isBlank()) {
            throw IllegalStateException("Invalid update information received")
        }

        return dasMediaHub
    }
}
