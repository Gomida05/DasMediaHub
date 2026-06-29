package com.das.mediaHub.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.das.mediaHub.MainActivity
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing media-related dependencies, such as ExoPlayer and MediaSession.
 */
@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    /**
     * Provides a configured instance of [ExoPlayer].
     * Configured for general media usage with network wake mode and audio focus handling.
     */
    @Provides
    fun provideJustExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    /**
     * Provides a [MediaSession] instance for the given [Player].
     * Sets the session activity to launch [MainActivity].
     */
    @Provides
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: Player
    ): MediaSession {
        return MediaSession.Builder(context, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    /**
     * Provides a singleton [Player] instance (using [ExoPlayer]).
     * Configured for movie playback with network wake mode and audio focus handling.
     */
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): Player {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(), true
            )
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    /**
     * Provides the singleton [VideoPlayerManager] for controlling video playback.
     */
    @Provides
    @Singleton
    fun provideVideoPlayerManager(
        player: Player
    ): VideoPlayerManager {
        return VideoPlayerManager(player)
    }
}
