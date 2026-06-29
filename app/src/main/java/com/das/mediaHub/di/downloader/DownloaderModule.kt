package com.das.mediaHub.di.downloader

import android.content.Context
import androidx.work.WorkManager
import com.das.downloader.AppUpdateRepository
import com.das.downloader.DownloadCoordinator
import com.das.downloader.DownloadNotifier
import com.das.downloader.DownloadQueueManager
import com.das.downloader.data.downloader.Downloader
import com.das.downloader.data.downloader.DownloaderRepo
import com.das.downloader.data.downloader.ResumableDownloader
import com.das.downloader.data.network.NetworkStatusProvider
import com.das.mediaHub.network.AndroidNetworkStatusProvider
import com.das.downloader.data.local.DownloadStateStore
import com.das.downloader.data.repository.MediaDownloadRepository
import com.das.downloader.data.repository.MediaDownloadRepositoryImpl
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

/**
 * Hilt module for providing downloader-related dependencies.
 * This includes components for managing download queues, state, notifications, and app updates.
 */
@Module
@InstallIn(SingletonComponent::class)
object DownloaderModule {

    /**
     * Provides a singleton [CoroutineScope] tied to the application's lifecycle.
     * Used for background tasks in the downloader module.
     */
    @Provides
    @Singleton
    @AppScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Provides a singleton [DownloadStateStore] for persisting download states.
     */
    @Provides
    @Singleton
    fun provideDownloadStateStore(@ApplicationContext context: Context): DownloadStateStore {
        val prefs = context.getSharedPreferences("download_state_store", Context.MODE_PRIVATE)
        return DownloadStateStore(prefs)
    }

    /**
     * Provides a [Downloader] implementation.
     * Uses [ResumableDownloader] with the provided [HttpClient].
     */
    @Provides
    fun provideResumableDownloader(client: HttpClient): Downloader = ResumableDownloader(client = client)

    /**
     * Provides the singleton [NetworkStatusProvider] implementation.
     */
    @Provides
    @Singleton
    fun provideNetworkStatusProvider(impl: AndroidNetworkStatusProvider): NetworkStatusProvider = impl

    /**
     * Provides the singleton [DownloadQueueManager] which manages the lifecycle of downloads.
     */
    @Provides
    @Singleton
    fun provideDownloadQueueManager(
        @ApplicationContext context: Context,
        store: DownloadStateStore,
        downloader: Downloader,
        networkProvider: NetworkStatusProvider,
        @AppScope scope: CoroutineScope
    ): DownloadQueueManager {
        return DownloadQueueManager(
            context = context,
            store = store,
            downloader = downloader,
            networkProvider = networkProvider,
            scope = scope
        )
    }

    /**
     * Provides the singleton [DownloaderRepo] for accessing download-related data and operations.
     */
    @Provides
    @Singleton
    fun provideDownloaderRepo(
        @ApplicationContext context: Context,
        queue: DownloadQueueManager
    ): DownloaderRepo {
        return DownloaderRepo(
            queue = queue,
            context = context
        )
    }

    /**
     * Provides the singleton [DownloadCoordinator] to orchestrate download tasks.
     */
    @Provides
    @Singleton
    fun provideDownloadCoordinator(repo: DownloaderRepo): DownloadCoordinator {
        return DownloadCoordinator(repo)
    }

    /**
     * Provides the singleton [DownloadNotifier] for showing download-related notifications.
     */
    @Provides
    @Singleton
    fun provideDownloadNotifier(@ApplicationContext context: Context): DownloadNotifier {
        return DownloadNotifier(context = context)
    }

    /**
     * Provides the singleton [AppUpdateRepository] for checking and managing app updates.
     */
    @Provides
    @Singleton
    fun provideAppUpdateRepo(httpClient: HttpClient): AppUpdateRepository =
        AppUpdateRepository(remoteUrl = APP_URL, client = httpClient)

    /**
     * Provides the [MediaDownloadRepository] for resolving media download tasks.
     */
    @Provides
    @Singleton
    fun provideMediaDownloadRepository(@ApplicationContext context: Context): MediaDownloadRepository {
        return MediaDownloadRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideWorker(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

}
