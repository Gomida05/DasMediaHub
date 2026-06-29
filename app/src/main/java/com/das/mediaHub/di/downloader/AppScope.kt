package com.das.mediaHub.di.downloader

import javax.inject.Qualifier

/**
 * Dagger Qualifier used to identify a [kotlinx.coroutines.CoroutineScope]
 * that is tied to the application's lifecycle.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScope
