package com.das.mediaHub

import android.content.Intent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.das.mediaHub.navigation.NavScreens.Home
import com.das.mediaHub.navigation.BottomNavItems
import com.das.mediaHub.navigation.AppNavHost
import com.das.mediaHub.ui.TopPopupNotification.Notification

@Composable
fun MainApp(
    pendingIntent: Intent?,
    onIntentConsumed: () -> Unit,
    onHandleIntent: (Intent, NavBackStack<NavKey>) -> Unit
) {
    val backStack = rememberNavBackStack(Home)

    LaunchedEffect(pendingIntent) {
        pendingIntent?.let {
            onHandleIntent(it, backStack)
            onIntentConsumed()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize(),
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