package com.das.mediaHub.ui.settings

import android.content.Context
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.das.mediaHub.NavScreens
import com.das.mediaHub.OnLaunchComponents.openCustomTab
import com.das.mediaHub.downloader.DownloaderClass
import com.das.mediaHub.data.model.AppUpdateInfo
import com.das.mediaHub.data.model.TopPopUp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth


@Composable
fun NavController.SettingsComposable(
    showPopMessage: (TopPopUp)-> Unit
) {

    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = viewModel<SettingsViewModel>()

    val auth = Firebase.auth

    var showDialog by rememberSaveable { mutableStateOf(false) }

    val isLoading by viewModel.isLoading
    val error by viewModel.foundError
    val appInfo by viewModel.apkInfo

    val snackBarHostState = remember { SnackbarHostState() }

    val url = remember { "https://gomida05.github.io/".toUri() }

    if (isLoading) {
        LoadingDialog {
            viewModel.cancelLoading()
        }
    }

    ErrorDialog(message = error,
        onDismiss = {
            viewModel.clearError()
        }
    ) {
        viewModel.retryLoad()
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            LargeTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = "Setting",
                        style = MaterialTheme.typography.headlineLarge
                            .copy(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) {
        LazyColumn(
            contentPadding = it,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            auth.currentUser?.let { user ->
                item {
                    UserHeader(
                        user
                    ) {
                        navigate(NavScreens.AccountSetting.route)
                    }
                }


                item { HorizontalDivider() }
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Save,
                    text = "Saved Videos",
                    onClick = { navigate("saved") }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.ColorLens,
                    text = "Appearance",
                    onClick = { navigate("user Setting") }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Update,
                    text = "Check for Update",
                    onClick = {
                        viewModel.loadJson()
                        showDialog = true
                    }
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Feedback,
                    text = "Send Feedback",
                    onClick = { navigate(NavScreens.FeedbackScreen.route) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    text = "About Us",
                    onClick = {
                        context.openCustomTab(url)
                    }
                )
            }

            item { HorizontalDivider() }
            item {
                AppVersionInfo()
            }
        }
    }

    if(showDialog && !appInfo.isEmpty()){
        ShowAlertDialog(
            context = context,
            appInfo = appInfo,
            snackBar = snackBarHostState,
            showPopMessage = {
                showPopMessage(
                    TopPopUp(
                        message = it,
                        icon = Icons.Default.Android
                    )
                )
            },
            onDismissRequest = {
                showDialog = false
            },
        )
    }

}

@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun LoadingDialog(
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
        title = {
            Text("Loading")
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Please wait while we fetch update info.")
            }
        }
    )
}

@Composable
fun ErrorDialog(
    message: String?,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {

    if (!message.isNullOrEmpty()){
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Row {
                    if (onRetry != null) {
                        TextButton(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            },
            title = { Text("Error") },
            text = {
                Text(text = message)
            }
        )
    }
}

@Composable
fun UserHeader(user: FirebaseUser, onClick: () -> Unit) {

    val name by rememberSaveable { mutableStateOf(user.displayName ?: "Sign in/Up now") }
    val email by rememberSaveable { mutableStateOf(user.email ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(22))
            .clickable{
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Text(text = email, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun AppVersionInfo() {
    val context = LocalContext.current
    val version = remember {
        context.packageManager
            .getPackageInfo(context.packageName, 0).versionName
    }
    Text(
        text = "App Version: $version",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}



@Composable
fun ShowAlertDialog(
    context: Context,
    appInfo: AppUpdateInfo,
    snackBar: SnackbarHostState,
    showPopMessage: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    // Get current version info
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        packageInfo.longVersionCode
    else
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()

    val currentVersionName = try {
        packageInfo.versionName?.toDoubleOrNull()
    } catch (_: Exception) {
        null
    }

    val isNewerVersion = appInfo.versionCode > currentVersionCode ||
            (currentVersionName != null && (appInfo.versionName.toDoubleOrNull()
                ?: 0.0) > currentVersionName)

    // Show message if already up-to-date
    if (!isNewerVersion) {
        LaunchedEffect(Unit) {
            showPopMessage("You're up to date")
            snackBar.showSnackbar( "You're up to date")
            onDismissRequest()
        }
        return
    }


    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("Update Available: v${appInfo.versionName}")
        },
        text = {
            Text("Changelog:\n${appInfo.whatsNew}")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    DownloaderClass(context).downloadNewVersionAPK(appInfo)
                    onDismissRequest()
                }
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}