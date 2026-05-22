package com.das.mediaHub.ui.players.videoPlayerLocally

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.das.mediaHub.services.media.PlaybackService
import com.google.common.util.concurrent.ListenableFuture

object PlayerControllerHolder {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null


    fun getOrCreate(
        context: Context,
        onReady: (MediaController) -> Unit
    ) {
        controller?.let {
            onReady(it)
            return
        }

        val future = controllerFuture ?: MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        ).buildAsync().also {
            controllerFuture = it
        }

        future.addListener(
            {
                try {
                    val built = future.get()
                    controller = built
                    onReady(built)   // <- missing
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun current(): MediaController? = controller

    fun release(context: Context) {
        controller?.release()
        controller = null
        controllerFuture = null
        context.stopService(Intent(context, PlaybackService::class.java))

    }
}