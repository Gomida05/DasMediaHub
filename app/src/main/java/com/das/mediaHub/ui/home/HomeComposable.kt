package com.das.mediaHub.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.das.mediaHub.NavScreens
import com.das.mediaHub.NavScreens.Searcher
import com.das.mediaHub.NavScreens.Downloads
import com.das.mediaHub.data.icons.Instagram
import com.das.mediaHub.data.icons.TikTok
import com.das.mediaHub.data.icons.YouTube
import com.das.mediaHub.ui.theme.CustomTheme
import kotlinx.coroutines.launch

@Preview
@Composable
fun OPreview() {
    CustomTheme {
        rememberNavController().HomePageComposable()
    }
}
@Composable

fun NavController.HomePageComposable() {
    val scope = rememberCoroutineScope()
    val snackBar = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackBar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DasMediaHub",
                        style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigate(NavScreens.AccountSetting.route)
                        }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User account"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navigate(Downloads.route) }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Downloaded medias"
                        )
                    }
                })
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 20.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Welcome to",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
                Text(
                    text = "DasMediaHub",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) // Quick platform icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    PlatformIcon(icon = TikTok, "TikTok") {
                        scope.launch {
                            snackBar.showSnackbar(
                                message = "Coming soon",
                                withDismissAction = true
                            )
                        }
                    }
                    PlatformIcon(icon = YouTube, "YouTube") { navigate(Searcher.route) }
                    PlatformIcon(icon = Instagram, "Instagram") {
                        scope.launch {
                            snackBar.showSnackbar(
                                message = "Coming soon",
                                withDismissAction = true
                            )
                        }
                    }
                }
                /*  FilledTonalButton(
                onClick = { navigate(Searcher.route) },
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.padding(end = 8.dp).size(22.dp)
                ) Text ("Search or Paste URL")
                }
                 **/
            }
        }

    }
}
@Composable
fun PlatformIcon(icon: ImageVector, label: String, onClick: ()-> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}



@Composable
fun NavController.HomePageComposable(setTopBar: (@Composable () -> Unit) -> Unit) {

    setTopBar {
        TopAppBar(
            title = {
                Text(
                    text = "DasMediaHub",
                    style = MaterialTheme.typography.titleLarge
                        .copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navigate(NavScreens.AccountSetting.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User account"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navigate(Downloads.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloaded medias"
                    )
                }
            }
        )
    }

    val scope = rememberCoroutineScope()
    val snackBar = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )

        Text(
            text = "DasMediaHub",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlatformIcon(icon = TikTok, "TikTok") {
                scope.launch { snackBar.showSnackbar("Coming soon") }
            }
            PlatformIcon(icon = YouTube, "YouTube") {
                navigate(Searcher.route)
            }
            PlatformIcon(icon = Instagram, "Instagram") {
                scope.launch { snackBar.showSnackbar("Coming soon") }
            }
        }
    }
}
