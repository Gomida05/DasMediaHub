package com.das.forui.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_ADD_TO_WATCH_LATER
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_KILL
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_NEXT
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_PAUSE
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_PLAY
import com.das.forui.services.AudioServiceFromUrl.Companion.ACTION_PREVIOUS
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback

class NotificationMediaDescriptionAdapter(private val metadataCompat: MediaControllerCompat, val context: Context): PlayerNotificationManager.MediaDescriptionAdapter {
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
        Glide.with(context)
            .asBitmap()
            .load(metadataCompat.metadata.description.iconUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    callback.onBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
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
        val playAction = NotificationCompat.Action(
            R.drawable.play_arrow_24dp, // Play icon
            "Play", // Label for the action
            createPendingIntent(context, ACTION_PLAY) // PendingIntent for the action
        )

        val pauseAction = NotificationCompat.Action(
            R.drawable.pause_icon, // Pause icon
            "Pause", // Label for the action
            createPendingIntent(context, ACTION_PAUSE) // PendingIntent for the action
        )

        val nextAction = NotificationCompat.Action(
            R.drawable.skip_next_24dp, // Next icon
            "Next", // Label for the action
            createPendingIntent(context, ACTION_NEXT) // PendingIntent for the action
        )

        val previousAction = NotificationCompat.Action(
            R.drawable.skip_previous_24dp, // Previous icon
            "Previous", // Label for the action
            createPendingIntent(context, ACTION_PREVIOUS) // PendingIntent for the action
        )
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
            ACTION_PLAY to playAction,
            ACTION_PAUSE to pauseAction,
            ACTION_NEXT to nextAction,
            ACTION_PREVIOUS to previousAction,
            ACTION_ADD_TO_WATCH_LATER to addToWatchLater,
            ACTION_KILL to killAction
        )
    }

    override fun getCustomActions(player: Player): MutableList<String> {
        return mutableListOf(ACTION_PLAY, ACTION_PAUSE, ACTION_NEXT, ACTION_PREVIOUS, ACTION_KILL)
    }

    override fun onCustomAction(player: Player, action: String, intent: Intent) {
        when (action) {
            ACTION_PLAY -> {
            }
            ACTION_PAUSE -> {
            }
            ACTION_PREVIOUS -> {
            }
            ACTION_NEXT -> {
            }
            ACTION_ADD_TO_WATCH_LATER -> {
            }
            ACTION_KILL -> {
                val notificationManager = context.getSystemService(NotificationManager::class.java)
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

class NotificationListenerService: PlayerNotificationManager.NotificationListener{

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
    }


    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
    }
}