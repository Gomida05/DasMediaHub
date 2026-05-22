package com.das.mediaHub.services.download

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.das.downloader.data.downloader.DownloadCoordinator
import com.das.downloader.data.downloader.DownloadNotifier
import com.das.downloader.data.downloader.DownloadQueueManager
import com.das.downloader.data.model.AppUpdateInfo
import com.das.downloader.data.model.download.DownloadType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : LifecycleService() {

    @Inject
    lateinit var queue: DownloadQueueManager

    @Inject
    lateinit var coordinator: DownloadCoordinator

    @Inject
    lateinit var notifier: DownloadNotifier

    override fun onCreate() {
        super.onCreate()




        lifecycleScope.launch {
            queue.restore()

            queue.states.collectLatest { states ->
                states.forEach { notifier.notifyState(it) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

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

                lifecycleScope.launch(Dispatchers.IO) {
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
                                    apkUrl = intent.getStringExtra(EXTRA_APK_URL) ?: "",
                                    latestVersionName = intent.getStringExtra(EXTRA_VERSION_NAME) ?: "",
                                    latestVersionCode = intent.getIntExtra(EXTRA_VERSION_CODE, 0),
                                    changelog = intent.getStringExtra(EXTRA_WHATS_NEW) ?: ""
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
        startForeground(
            FOREGROUND_NOTIFICATION_ID, notifier.foregroundNotification()
        )
        return START_STICKY
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
            context.startService(intent)
        }

        fun pause(context: Context, taskId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_PAUSE
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startService(intent)
        }

        fun resume(context: Context, taskId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_RESUME
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startService(intent)
        }

        fun cancel(context: Context, taskId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startService(intent)
        }

        fun startForApk(context: Context, appInfo: AppUpdateInfo) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_APK_URL, appInfo.apkUrl)
                putExtra(EXTRA_VERSION_NAME, appInfo.latestVersionName)
                putExtra(EXTRA_VERSION_CODE, appInfo.latestVersionCode)
                putExtra(EXTRA_WHATS_NEW, appInfo.changelog)
                putExtra(EXTRA_TITLE, "App Update")
                putExtra(EXTRA_TYPE, DownloadType.APK.name)
            }
            context.startService(intent)
        }


    }
}