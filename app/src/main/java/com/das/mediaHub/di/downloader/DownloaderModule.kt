package com.das.mediaHub.di.downloader

import android.content.Context
import android.os.Environment
import com.das.downloader.AppUpdateRepository
import com.das.downloader.data.downloader.DownloadCoordinator
import com.das.downloader.data.downloader.DownloadNotifier
import com.das.downloader.data.downloader.DownloadQueueManager
import com.das.downloader.data.downloader.Downloader
import com.das.downloader.data.downloader.DownloaderRepo
import com.das.downloader.data.downloader.ResumableDownloader
import com.das.downloader.data.local.DownloadStateStore
import com.das.mediaHub.data.constants.UrlLists.APP_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloaderModule {

    @Provides
    @Singleton
    @AppScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDownloadStateStore(@ApplicationContext context: Context): DownloadStateStore {
        val prefs = context.getSharedPreferences("download_state_store", Context.MODE_PRIVATE)
        return DownloadStateStore(prefs)
    }

    @Provides
    fun provideResumableDownloader(client: HttpClient): Downloader = ResumableDownloader(client = client)

    @Provides
    @Singleton
    fun provideDownloadQueueManager(
        store: DownloadStateStore,
        downloader: Downloader,
        @AppScope scope: CoroutineScope
    ): DownloadQueueManager {
        return DownloadQueueManager(store = store, downloader = downloader, scope = scope)
    }

    @Provides
    @Singleton
    fun provideDownloaderRepo(
        @ApplicationContext context: Context,
        queue: DownloadQueueManager
    ): DownloaderRepo {
        // Resolve your custom storage paths from the App side dynamically
        val videoPath = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath ?: ""
        val audioPath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: ""
        val apkPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath ?: context.filesDir.absolutePath
        val appName = context.applicationInfo.loadLabel(context.packageManager).toString().ifBlank { "MediaHub" }

        return DownloaderRepo(
            queue = queue,
            videoPath = videoPath,
            audioPath = audioPath,
            apkPath = apkPath,
            appName = appName
        )
    }

    @Provides
    @Singleton
    fun provideDownloadCoordinator(repo: DownloaderRepo): DownloadCoordinator {
        return DownloadCoordinator(repo)
    }


    @Provides
    @Singleton
    fun provideDownloadNotifier(@ApplicationContext context: Context): DownloadNotifier {
        return DownloadNotifier(context = context)
    }

    @Provides
    @Singleton
    fun provideAppUpdateRepo(httpClient: HttpClient): AppUpdateRepository =
        AppUpdateRepository(remoteUrl = APP_URL, client = httpClient)

}