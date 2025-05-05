package com.das.forui.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.das.forui.objectsAndData.ForUIKeyWords.AUDIO_SERVICE_FROM_URL_NOTIFICATION
import com.das.forui.objectsAndData.ForUIKeyWords.BACKGROUND_GROUND_PLAYER_NOTIFICATION

class BroadReceiverForNotificationActivity: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == AUDIO_SERVICE_FROM_URL_NOTIFICATION) {
            context?.stopService(Intent(context, AudioServiceFromUrl::class.java))
        }
        if (intent?.action == BACKGROUND_GROUND_PLAYER_NOTIFICATION){
            context?.stopService(Intent(context, BackGroundPlayer::class.java))
        }
    }
}