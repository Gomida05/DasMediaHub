package com.das.mediaHub.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.das.mediaHub.NavScreens
import com.das.mediaHub.downloader.DownloaderClass
import com.das.mediaHub.data.model.AppUpdateInfo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch


@Composable
fun SettingsComposable(navController: NavController) {

    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = viewModel<SettingsViewModel>()

    val auth = Firebase.auth
    val isUserLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    var showDialog by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading
    val error by viewModel.foundError
    val appInfo by viewModel.apkInfo

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (isLoading) {
        LoadingDialog {
            viewModel.cancelLoading()
        }
    }

    if (!error.isNullOrEmpty()) {
        ErrorDialog(error.toString(), onDismiss = {
            viewModel.clearError()
        }) {
            viewModel.retryLoad()
        }
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
            modifier = Modifier
                .fillMaxSize()
        ) {

            if (isUserLoggedIn) {
                item { UserHeader() }
            }

            item {
                VerticalDivider(modifier = Modifier.padding(vertical = 3.dp))
            }

            item { Saved(navController) }
            item { Account(context) }


            item {
                VerticalDivider(modifier = Modifier.padding(vertical = 1.dp))
            }

            item { Appearance(navController) }

            item {
                VerticalDivider(modifier = Modifier.padding(vertical = 1.dp))
            }

            item {
                Check_for_update {
                    scope.launch {
                        snackBarHostState.showSnackbar("Downloading app")
                    }
                    viewModel.loadJson()
                    showDialog = true
                }
            }
            item { About_Us(context) }

            item { FeedbackButton(navController) }
            item { AppVersionInfo() }

        }
    }

    if(showDialog && !appInfo.isEmpty()){
        ShowAlertDialog(
            context = context,
            appInfo = appInfo,
            snackBar = snackBarHostState,
            onDismissRequest = {
                showDialog = false
            },
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
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
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

@Composable
fun UserHeader() {
    val auth = Firebase.auth

    val name by remember { mutableStateOf(auth.currentUser?.displayName ?: "Guest") }
    val email by remember { mutableStateOf(auth.currentUser?.email?: "Coming soon") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
private fun Saved(navController: NavController){
    Card(
        onClick = {
            navController.navigate("saved")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(25))

    ) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                "",
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
            Text(
                text = "Saved Videos",
                fontSize = 16.sp,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                "",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

    }
}

@Composable
private fun Account(context: Context) {
    Card(
        onClick = {
            openMusicApp(context)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(25))

    ) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                "",
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
            Text(
                text = "Account",
                fontSize = 16.sp,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                "",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

    }
}



@Composable
private fun Check_for_update(onClick: () -> Unit){

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(30))

    ) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Update,
                "",
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
            Text(
                text = "Check for update",
                fontSize = 16.sp,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                "",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

    }




}

@Composable
private fun Appearance(navController: NavController){
    Card(
        onClick = {
            navController.navigate("user Setting")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(30))

    ) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ColorLens,
                "",
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
            Text(
                text = "Appearance",
                fontSize = 16.sp,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                "",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

    }
}

@Composable
private fun About_Us(mContext: Context){
    Card(
        onClick = {
            goToWeb(mContext)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(30))

    ) {


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                "",
                modifier = Modifier
                    .align(Alignment.CenterStart)
            )
            Text(
                text = "About US",
                fontSize = 16.sp,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                "",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

    }
}


@Composable
fun FeedbackButton(navController: NavController) {
    Card(
        onClick = {
            navController.navigate(NavScreens.FeedbackScreen.route)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(25))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Feedback,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
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

private fun goToWeb(mContext: Context) {

    val url = "https://gomida05.github.io/".toUri()

    val browserIntent = Intent(Intent.ACTION_VIEW, url)

    mContext.startActivity(browserIntent)
}





private fun showDialogs(context: Context, inputText: String = "coming soon") {
    Toast.makeText(context, inputText, Toast.LENGTH_SHORT).show()
}


fun openMusicApp(context: Context){
    try {
        val musicApp = context.packageManager.getLaunchIntentForPackage("com.das.musicplayer")
        context.startActivity(musicApp)
    }catch (_: PackageManager.NameNotFoundException){
        showDialogs(context, "App Not founded!")
    }
    catch (_: Exception){
        showDialogs(context,"App not opening!")
    }
}


