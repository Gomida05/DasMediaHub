package com.das.mediaHub.services.download

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.das.downloader.AppUpdateRepository
import com.das.mediaHub.NotificationChannels.NotificationChannelNames.DOWNLOAD_CHANNEL
import com.das.mediaHub.R
import com.das.mediaHub.data.local.UpdatePreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * A [CoroutineWorker] that handles the background download of the application APK update.
 *
 * This worker retrieves the latest update information from the [AppUpdateRepository],
 * downloads the binary file using [HttpClient], and stores it in the device's external
 * storage directory. Once completed, it triggers a system notification that, when tapped,
 * initiates the installation process via the system's package installer.
 *
 * Example usage from a ViewModel or Activity:
 * ```kotlin
 * val downloadWork = OneTimeWorkRequestBuilder<DownloadAPK>()
 *     .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
 *     .build()
 * WorkManager.getInstance(context).enqueue(downloadWork)
 * ```
 */
@HiltWorker
class DownloadAPK @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val httpClient: HttpClient,
    private val appUpdateRepo: AppUpdateRepository,
    private val updatePreferences: UpdatePreferences
) : CoroutineWorker(appContext = appContext, params = params) {
    private val notificationManager by lazy {
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch latest update info to get the APK URL
            val updateInfo = appUpdateRepo.checkForUpdates()
            val apkUrl = updateInfo.apkUrl
            val latestVersion = updateInfo.latestVersionCode

            if (apkUrl.isBlank()) {
                Log.e("DownloadAPK", "Update check succeeded but APK URL is blank.")
                return@withContext Result.failure()
            }

            // 2. Define the destination file in the Downloads directory
            val destinationFile = File(
                appContext.getExternalFilesDir(null),
                "DasMediaHub_Update_$latestVersion.apk"
            )

            // 3. Set foreground status to prevent the system from killing the worker
            setForeground(createInitialForegroundInfo())

            // 4. Execute the download request
            val response = httpClient.get(apkUrl) {
                timeout {
                    connectTimeoutMillis = 15_000
                    requestTimeoutMillis = 5 * 60 * 1000 // 5 minutes
                    socketTimeoutMillis = 60_000
                }
            }

            if (!response.status.isSuccess()) {
                Log.e("DownloadAPK", "Download failed: ${response.status}")
                return@withContext Result.retry()
            }
            val totalBytes = response.contentLength() ?: -1L
            val channel = response.bodyAsChannel()

            // 5. Stream the content to the file with progress updates
            FileOutputStream(destinationFile).use { output ->
                if (totalBytes > 0) {
                    val buffer = ByteArray(8192)
                    var bytesReadTotal = 0L
                    var lastProgress = 0

                    var lastUpdateTime = 0L

                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        bytesReadTotal += read

                        val progress = ((bytesReadTotal * 100) / totalBytes).toInt()
                        val now = System.currentTimeMillis()

                        if (progress != lastProgress && now - lastUpdateTime >= 500) {
                            lastProgress = progress
                            lastUpdateTime = now
                            notificationManager.notify(FOREGROUND_NOTIFICATION_ID, buildProgressNotification(progress))
                        }
                    }
                } else {
                    // Fallback if content length is unknown (indeterminate progress)
                    val buffer = ByteArray(8192)
                    while (!channel.isClosedForRead) {
                        val read = channel.readAvailable(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                    }
                }
            }

            // 6. Notify the user that the update is ready to install
            updatePreferences.updatePendingInstall(
                apkPath = destinationFile.absolutePath,
                versionCode = latestVersion
            )
            showFinishedNotification(destinationFile.absolutePath)

            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadAPK", "Error during APK download: ${e.message}", e)
            cancelNotification()
            Result.failure()
        }
    }

    /**
     * Creates the [ForegroundInfo] required for a long-running [CoroutineWorker].
     * Shows a notification with an indeterminate progress bar.
     */
    private fun createInitialForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(appContext, DOWNLOAD_CHANNEL)
            .setContentTitle("Preparing download…")
            .setContentText("Starting update download")
            .setSmallIcon(R.drawable.download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true) // indeterminate
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    /**
     * Clicking this notification directly initiates the APK installation process.
     *
     * @param apkPath The absolute path to the downloaded APK file.
     */
    private fun showFinishedNotification(apkPath: String) {
        val file = File(apkPath)
        val apkUri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.provider",
            file
        )

        // We use ACTION_VIEW for the APK. If the "Install unknown apps" permission 
        // is missing, the system automatically redirects the user to settings 
        // and returns them to the installation prompt after they grant it.
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check if we can request package installations.
        val canInstall = appContext.packageManager.canRequestPackageInstalls()

        val title = if (!canInstall) {
            "Update Ready: Permission Needed"
        } else {
            "Update Ready to Install"
        }

        val content = if (!canInstall) {
            "Download finished. Tap to allow installation and update the app."
        } else {
            "The new version of DasMediaHub has been downloaded. Tap to install."
        }

        val notification = NotificationCompat.Builder(appContext, DOWNLOAD_CHANNEL)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.download)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()


        notificationManager.notify(COMPLETE_NOTIFICATION_ID, notification)
    }

    private fun buildProgressNotification(progress: Int): Notification {
        return NotificationCompat.Builder(appContext, DOWNLOAD_CHANNEL)
            .setContentTitle("Downloading App Update")
            .setContentText("$progress%")
            .setSmallIcon(R.drawable.download)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()
    }

    /**
     * Cancels the ongoing download notification.
     */
    private fun cancelNotification() {
        notificationManager.cancel(FOREGROUND_NOTIFICATION_ID)
    }

    companion object {
        /** Unique identifier for the update download notification. */
        private const val FOREGROUND_NOTIFICATION_ID = 110022
        /** Unique identifier for the completed download notification. */
        private const val COMPLETE_NOTIFICATION_ID = 110023
    }
}
