package com.das.mediaHub.di

import android.content.Context
import android.net.ConnectivityManager
import com.das.downloader.exception.NetworkRequestException
import com.das.mediaHub.network.ConnectivityObserver
import com.das.mediaHub.network.NetworkObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        connectivityManager: ConnectivityManager
    ): ConnectivityObserver {
        return NetworkObserver(connectivityManager)
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(HttpTimeout)

            install(ContentNegotiation) {
                json(
                    Json {
                        encodeDefaults = true
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                        isLenient = true
                    }
                )
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess() && response.status.value != 206 && response.status.value != 416) {
                        val errorMsg =
                            runCatching { response.bodyAsText() }.getOrElse { "Request failed" }
                        throw NetworkRequestException(response.status.value, errorMsg)
                    }
                }
            }
        }
    }


    @Provides
    @Singleton
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}