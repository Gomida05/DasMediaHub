package com.das.mediaHub.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.das.mediaHub.NavScreens
import com.das.mediaHub.NavScreens.Searcher
import com.das.mediaHub.NavScreens.Downloads


@Composable
fun HomePageComposable(navController: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                        Text(
                            text = "DasMediaHub",
                            style = MaterialTheme.typography.headlineMedium
                                .copy(textAlign = TextAlign.Center),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                },
                navigationIcon = {
                    ElevatedButton(
                        onClick = {
                            navController.navigate(NavScreens.LoginPage1.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    ElevatedButton(
                        onClick = {
                            navController.navigate(Downloads.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloads",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Button(
                onClick = {
                    navController.navigate(Searcher.route)
                },
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 300.dp, height = 60.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(22.dp)
                )

                Text(
                    text = "Search or Paste URL",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
