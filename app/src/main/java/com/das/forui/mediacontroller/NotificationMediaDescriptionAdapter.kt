@file:Suppress("DEPRECATION")
package com.das.forui.mediacontroller

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.forui.R
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_ADD_TO_WATCH_LATER
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_KILL
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class NotificationMediaDescriptionAdapter(private val context: Context, private val metadataCompat: MediaControllerCompat): PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return metadataCompat.metadata.description.title.toString()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return metadataCompat.sessionActivity
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return metadataCompat.metadata.description.subtitle
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val iconUrl = metadataCompat.metadata.description.iconUri
        Glide.with(context)
            .asBitmap()
            .load("https://img.youtube.com/vi/${iconUrl}/0.jpg")
            .into(object : CustomTarget<Bitmap>() {

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    callback.onBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
//                    Toast.makeText(context, "Well well well", Toast.LENGTH_SHORT).show()
                }

            }
            )
        return null
    }


}
class NotificationCustomActions(private val context: Context): PlayerNotificationManager.CustomActionReceiver{
    override fun createCustomActions(
        context: Context,
        instanceId: Int
    ): MutableMap<String, NotificationCompat.Action> {

        val addToWatchLater = NotificationCompat.Action(
            R.drawable.favorite,
            "Fav",
            createPendingIntent(context, ACTION_ADD_TO_WATCH_LATER)
        )

        val killAction = NotificationCompat.Action(
            R.drawable.stop_circle_24dp, // Stop icon
            "Stop", // Label for the action
            createPendingIntent(context, ACTION_KILL) // PendingIntent for the action
        )

        return mutableMapOf(
            ACTION_ADD_TO_WATCH_LATER to addToWatchLater,
            ACTION_KILL to killAction
        )
    }

    override fun getCustomActions(player: Player): MutableList<String> {
        return mutableListOf(ACTION_ADD_TO_WATCH_LATER, ACTION_KILL)
    }


    override fun onCustomAction(player: Player, action: String, intent: Intent) {
        when (action) {
            ACTION_ADD_TO_WATCH_LATER -> {
            }
            ACTION_KILL -> {
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.cancel(1)
            }
        }
    }


    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(action)
        return PendingIntent.getService(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class NotificationListenerService(private val player: ExoPlayer?,
    private val mediaSessionCompat: MediaSessionCompat
): PlayerNotificationManager.NotificationListener{

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        mediaSessionCompat.release()
        super.onNotificationCancelled(notificationId, dismissedByUser)

    }


    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, true)
    }
}