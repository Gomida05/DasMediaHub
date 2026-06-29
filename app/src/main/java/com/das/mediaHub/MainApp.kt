package com.das.mediaHub

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkWifi1Bar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.navigation.AppBackStack
import com.das.mediaHub.navigation.AppNavHost
import com.das.mediaHub.navigation.BottomNavItems
import com.das.mediaHub.navigation.Destination.Home
import com.das.mediaHub.network.ConnectivityObserver
import com.das.mediaHub.network.NoConnectionScreen
import com.das.mediaHub.network.toUiMessage
import com.das.mediaHub.ui.notification.TopPopupNotification.Notification
import com.das.mediaHub.ui.notification.TopPopupNotification.showNotificationDialog

@Composable
fun MainApp(
    status: ConnectivityObserver.Status,
    pendingIntent: Intent?,
    onHandleIntent: (Intent, AppBackStack) -> Unit,
    openNetworkSetting: () -> Unit,
    onShowMainUpdateDialog: () -> Unit
) {
    val backStack = rememberNavBackStack(Home)

    var showDialog by retain { mutableStateOf(false) }


    LaunchedEffect(pendingIntent) {
        pendingIntent?.let {
            onHandleIntent(it, backStack)
        }
    }

    LaunchedEffect(status) {
        if (status == ConnectivityObserver.Status.Losing) {
            showNotificationDialog = TopPopUp(
                message = status.toUiMessage(),
                icon = Icons.Default.NetworkWifi1Bar
            )
        } else {
            showNotificationDialog = null
            showDialog =
                status == ConnectivityObserver.Status.Lost || status == ConnectivityObserver.Status.Unavailable
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
            paddingValues = paddingValues,
            onShowMainUpdateDialog = onShowMainUpdateDialog
        )

        if (showDialog) {
            NoConnectionScreen(networkStatus = status) {
                openNetworkSetting()
                showDialog = false
            }
        }
    }
}