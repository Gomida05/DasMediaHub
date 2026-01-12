package com.das.mediaHub.services.local

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.das.mediaHub.MainActivity
import com.das.mediaHub.R
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@SuppressLint("UnsafeOptInUsageError")
class DescriptionAdapter(val context: Context): MediaDescriptionAdapter {

    private var cachedArtwork: Bitmap? = null


    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.mediaMetadata.title ?: "Unknown"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return player.mediaMetadata.artist
    }

    override fun getCurrentSubText(player: Player): CharSequence? {
        return player.currentMediaItem?.mediaMetadata?.description
    }


    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {

        cachedArtwork?.let { return it }

        Glide.with(context)
            .asBitmap()
            .load(R.drawable.large_music_icon)
            .centerCrop()
            .override(256, 256)
            .into(object : CustomTarget<Bitmap>(256, 256) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    cachedArtwork = resource
                    callback.onBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        return cachedArtwork
    }


    private fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return BitmapFactory.decodeStream(ByteArrayInputStream(stream.toByteArray()))
    }
}