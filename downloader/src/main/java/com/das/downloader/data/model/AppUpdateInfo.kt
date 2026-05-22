package com.das.downloader.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing update information for the application.
 * 
 * Typically retrieved from a remote server to check if a new version
 * of the APK is available for download.
 * 
 * @property latestVersionCode The integer version code of the latest release.
 * @property latestVersionName The human-readable version string (e.g., "1.2.0").
 * @property apkUrl The direct URL to download the new APK.
 * @property changelog Description of changes and new features in this version.
 */
@Serializable
data class AppUpdateInfo(
    val latestVersionCode: Int = -1,
    val latestVersionName: String = "",
    val apkUrl: String = "",
    val changelog: String = ""
) {

    /**
     * Checks if this object contains valid update data.
     * @return true if the object is effectively empty.
     */
    fun isEmpty() = latestVersionCode == -1 && apkUrl.isEmpty()
}
