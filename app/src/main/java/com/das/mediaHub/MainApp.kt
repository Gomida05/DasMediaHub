package com.das.mediaHub

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.das.mediaHub.navigation.AppBackStack
import com.das.mediaHub.navigation.AppNavHost
import com.das.mediaHub.navigation.BottomNavItems
import com.das.mediaHub.navigation.Destination.Home
import com.das.mediaHub.network.NoConnectionScreen
import com.das.mediaHub.ui.notification.TopPopupNotification.Notification

@Composable
fun MainApp(
    isConnected: Boolean,
    pendingIntent: Intent?,
    onHandleIntent: (Intent, AppBackStack) -> Unit,
    openNetworkSetting: () -> Unit,
) {
    val backStack = rememberNavBackStack(Home)

    var showDialog by retain { mutableStateOf(false) }


    LaunchedEffect(pendingIntent) {
        pendingIntent?.let {
            onHandleIntent(it, backStack)
        }
    }

    LaunchedEffect(isConnected) {
        showDialog = !isConnected
    }

    if (!isConnected && showDialog) {
        NoConnectionScreen {
            openNetworkSetting()
            showDialog = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            BottomNavItems(backStack)
        }
    ) { paddingValues ->

        Notification()

        AppNavHost(
            backStack = backStack,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
    }
}