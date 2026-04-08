package com.das.mediaHub.services.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.das.downloader.data.model.AppUpdateInfo
import com.das.downloader.data.downloader.DownloadCoordinator
import com.das.downloader.data.downloader.DownloadNotifier
import com.das.downloader.data.downloader.DownloadQueueManager
import com.das.downloader.data.downloader.DownloaderRepo
import com.das.downloader.data.model.download.DownloadType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var queue: DownloadQueueManager
    private lateinit var notifier: DownloadNotifier
    private lateinit var coordinator: DownloadCoordinator

    override fun onCreate() {
        super.onCreate()
        queue = DownloadQueueManager.get(this)
        notifier = DownloadNotifier(this)
        coordinator = DownloadCoordinator(DownloaderRepo(this))

        startForeground(FOREGROUND_NOTIFICATION_ID, notifier.foregroundNotification())

        scope.launch {
            queue.restore()
        }

        scope.launch {
            queue.states.collectLatest { states ->
                states.forEach { notifier.notifyState(it) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_START -> {
                val sourceId = intent.getStringExtra(EXTRA_SOURCE_ID)
                val title = intent.getStringExtra(EXTRA_TITLE)
                val typeName = intent.getStringExtra(EXTRA_TYPE)

                if (sourceId.isNullOrBlank() || title.isNullOrBlank() || typeName.isNullOrBlank()) {
                    return START_NOT_STICKY
                }

                val type = runCatching { DownloadType.valueOf(typeName) }.getOrNull()
                    ?: return START_NOT_STICKY

                scope.launch {
                    when (type) {
                        DownloadType.VIDEO -> {
                            coordinator.enqueueVideoFromYoutube(
                                videoId = sourceId,
                                title = title,
                                onQueued = { },
                                onError = { }
                            )
                        }

                        DownloadType.MUSIC -> {
                            coordinator.enqueueMusicFromYoutube(
                                videoId = sourceId,
                                title = title,
                                onQueued = { },
                                onError = { }
                            )
                        }

                        DownloadType.TIKTOK_VIDEO -> {
                            val url = intent.getStringExtra(EXTRA_TIKTOK_URL) ?:""
                            coordinator.enqueueTiktokVideo(
                                url = url,
                                title = title
                            )
                        }

                        DownloadType.APK -> {
                            coordinator.downloadApk(
                                appInfo = AppUpdateInfo(
                                    appURL = intent.getStringExtra(EXTRA_APK_URL) ?: "",
                                    versionName = intent.getStringExtra(EXTRA_VERSION_NAME) ?: "",
                                    versionCode = intent.getIntExtra(EXTRA_VERSION_CODE, 0),
                                    whatsNew = intent.getStringExtra(EXTRA_WHATS_NEW) ?: ""
                                )
                            )
                        }
                    }
                }
            }

            ACTION_PAUSE -> {
                intent.getStringExtra(EXTRA_TASK_ID)?.let(queue::pause)
            }

            ACTION_RESUME -> {
                intent.getStringExtra(EXTRA_TASK_ID)?.let(queue::resume)
            }

            ACTION_CANCEL -> {
                intent.getStringExtra(EXTRA_TASK_ID)?.let(queue::cancel)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 991001

        const val ACTION_START = "com.das.mediaHub.download.START"
        const val ACTION_PAUSE = "com.das.mediaHub.download.PAUSE"
        const val ACTION_RESUME = "com.das.mediaHub.download.RESUME"
        const val ACTION_CANCEL = "com.das.mediaHub.download.CANCEL"

        const val EXTRA_SOURCE_ID = "sourceId"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TYPE = "type"
        const val EXTRA_TASK_ID = "taskId"
        const val EXTRA_TIKTOK_URL = "tiktokUrl"
        const val EXTRA_APK_URL = "apkUrl"
        const val EXTRA_VERSION_NAME = "versionName"
        const val EXTRA_VERSION_CODE = "versionCode"
        const val EXTRA_WHATS_NEW = "whatsNew"


        fun startForYouTube(
            context: Context,
            id: String,
            title: String,
            type: DownloadType
        ) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SOURCE_ID, id)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TYPE, type.name)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context, taskId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_PAUSE
                putExtra(EXTRA_TASK_ID, taskId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun resume(context: Context, taskId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_RESUME
                putExtra(EXTRA_TASK_ID, taskId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun cancel(context: Context, taskId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_TASK_ID, taskId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun startForApk(context: Context, appInfo: AppUpdateInfo) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_APK_URL, appInfo.appURL)
                putExtra(EXTRA_VERSION_NAME, appInfo.versionName)
                putExtra(EXTRA_VERSION_CODE, appInfo.versionCode)
                putExtra(EXTRA_WHATS_NEW, appInfo.whatsNew)
                putExtra(EXTRA_TYPE, "apk") // or "music"
            }
            ContextCompat.startForegroundService(context, intent)
        }


    }
}