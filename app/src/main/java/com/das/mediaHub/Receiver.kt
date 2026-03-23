package com.das.mediaHub

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.das.mediaHub.data.constants.Notifications
import com.das.mediaHub.services.media.LocalBackGroundPlayer
import com.das.mediaHub.services.media.OnlineBackgroundPlayer

class Receiver: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            Notifications.AUDIO_SERVICE_FROM_URL_NOTIFICATION -> {
                context.stopService(
                    Intent(context, OnlineBackgroundPlayer::class.java)
                )
            }
            Notifications.BACKGROUND_GROUND_PLAYER_NOTIFICATION -> {
                context.stopService(Intent(context, LocalBackGroundPlayer::class.java))
            }
        }
    }


}