package com.das.mediaHub.data.mediacontroller.local

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.media3.common.Player
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.mediaHub.MainActivity
import com.das.mediaHub.R

/**
 * Adapter responsible for providing metadata (title, text, icons) to the Media3 
 * [PlayerNotificationManager].
 *
 * This is used for local media playback notifications, ensuring they display the 
 * correct media information and provide an intent to open the app when clicked.
 */
@SuppressLint("UnsafeOptInUsageError")
class DescriptionAdapter(val context: Context): MediaDescriptionAdapter {

    private var bitmapCache: Bitmap? = null

    /** Retrieves the title from current media metadata. */
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.mediaMetadata.title ?: "Unknown"
    }

    /** Creates a [PendingIntent] to launch [MainActivity] when the notification is clicked. */
    override fun createCurrentContentIntent(player: Player): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Retrieves the artist/content text from media metadata. */
    override fun getCurrentContentText(player: Player): CharSequence? {
        return player.mediaMetadata.artist
    }

    /** Retrieves subtext from media metadata. */
    override fun getCurrentSubText(player: Player): CharSequence? {
        return player.currentMediaItem?.mediaMetadata?.description
    }

    /**
     * Loads and provides the large icon for the notification.
     * Uses Glide for asynchronous bitmap loading and includes a local cache.
     */
    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {

        bitmapCache?.let {
            callback.onBitmap(it)
            return it
        }

        Glide.with(context)
            .asBitmap()
            .load(R.drawable.large_music_icon)
            .fitCenter()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    bitmapCache = resource
                    callback.onBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        return bitmapCache
    }
}
