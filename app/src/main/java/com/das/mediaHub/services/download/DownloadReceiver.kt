package com.das.mediaHub.services.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.das.downloader.DownloadQueueManager
import com.das.downloader.data.downloader.DownloadRequest
import com.das.mediaHub.services.download.DownloadWorker.Companion.ACTION_CANCEL
import com.das.mediaHub.services.download.DownloadWorker.Companion.ACTION_PAUSE
import com.das.mediaHub.services.download.DownloadWorker.Companion.ACTION_RESUME
import com.das.mediaHub.services.download.DownloadWorker.Companion.ACTION_RETRY
import com.das.mediaHub.services.download.DownloadWorker.Companion.EXTRA_DOWNLOAD_REQUEST
import com.das.mediaHub.services.download.DownloadWorker.Companion.EXTRA_TASK_ID
import com.das.python.PythonMain.decodeStringToJson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadReceiver : BroadcastReceiver() {

    @Inject
    lateinit var queueManager: DownloadQueueManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_RETRY -> {
                val requestJson = intent.getStringExtra(EXTRA_DOWNLOAD_REQUEST) ?: return
                val request = requestJson.decodeStringToJson<DownloadRequest>()
                DownloadDispatcher.enqueue(context, request)
            }
            ACTION_CANCEL -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
                queueManager.cancel(taskId)
            }
            ACTION_PAUSE -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
                queueManager.pause(taskId)
            }
            ACTION_RESUME -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
                queueManager.resume(taskId)
            }
        }
    }
}
