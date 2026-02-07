package com.das.mediaHub.ui.settings

import android.content.Context
import android.content.pm.PackageInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.das.mediaHub.NavScreens
import com.das.mediaHub.OnLaunchComponents.openCustomTab
import com.das.mediaHub.downloader.DownloaderClass
import com.das.mediaHub.data.model.AppUpdateInfo
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsComposable(user: FirebaseUser?, add: (NavScreens) -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = viewModel<SettingsViewModel>()

    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }

    val showDialog = remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.foundError.collectAsState()
    val appInfo by viewModel.apkInfo.collectAsState()

    val url = remember { "https://gomida05.github.io/".toUri() }

    if (isLoading) {
        LoadingDialog { viewModel.cancelLoading() }
    }

    ErrorDialog(message = error,
        onDismiss = { viewModel.clearError() }
    ) {
        viewModel.retryLoad()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { padding ->
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                user?.let { user ->
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        UserHeader(user) { add(NavScreens.AccountSetting) }
                    }
                }

                item {
                    SettingsSection(title = "Media") {
                        SettingsItem(
                            icon = Icons.Default.Save,
                            text = "Saved Videos",
                            onClick = { add(NavScreens.Saved) }
                        )
                    }
                }

                item {
                    SettingsSection(title = "Personalization") {
                        SettingsItem(
                            icon = Icons.Default.ColorLens,
                            text = "Appearance",
                            onClick = { add(NavScreens.UserSettings) }
                        )
                    }
                }

                item {
                    SettingsSection(title = "App") {
                        SettingsItem(
                            icon = Icons.Default.Update,
                            text = "Check for Update",
                            onClick = {
                                viewModel.loadJson()
                                showDialog.value = true
                            }
                        )
                        SettingsItem(
                            icon = Icons.Default.Feedback,
                            text = "Send Feedback",
                            onClick = { add(NavScreens.FeedbackScreen) }
                        )
                        SettingsItem(
                            icon = Icons.Default.Info,
                            text = "About Us",
                            onClick = {
                                add(NavScreens.AboutDasMediaHub)
                            }
                        )
                    }
                }

                item {
                    packageInfo.AppVersionInfo()
                }
                
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    if (showDialog.value && !appInfo.isEmpty()) {
        ShowAlertDialog(
            context = context,
            appInfo = appInfo,
            packageInfo = packageInfo,
            onDismissRequest = { showDialog.value = false },
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 16.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun UserHeader(user: FirebaseUser, onClick: () -> Unit) {
    val name by rememberSaveable { mutableStateOf(user.displayName ?: "Sign in/Up now") }
    val email by rememberSaveable { mutableStateOf(user.email ?: "") }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (email.isNotEmpty()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PackageInfo.AppVersionInfo() {
    Text(
        text = "DasMediaHub v$versionName",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        textAlign = TextAlign.Center
    )
}

// ... LoadingDialog, ErrorDialog, ShowAlertDialog remain mostly the same but could be slightly styled if needed.
// Keeping them for functionality as requested.

@Composable
fun LoadingDialog(onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = { TextButton(onClick = onCancel) { Text("Cancel") } },
        title = { Text("Loading") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                Spacer(modifier = Modifier.width(16.dp))
                Text("Fetching update info...")
            }
        }
    )
}

@Composable
fun ErrorDialog(message: String?, onDismiss: () -> Unit, onRetry: (() -> Unit)? = null) {
    if (!message.isNullOrEmpty()){
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                Row {
                    if (onRetry != null) {
                        TextButton(onClick = onRetry) { Text("Retry") }
                    }
                    TextButton(onClick = onDismiss) { Text("Dismiss") }
                }
            },
            title = { Text("Error") },
            text = { Text(text = message) }
        )
    }
}

@Composable
fun ShowAlertDialog(context: Context, appInfo: AppUpdateInfo, packageInfo: PackageInfo, onDismissRequest: () -> Unit) {
    val currentVersionCode = remember { PackageInfoCompat.getLongVersionCode(packageInfo) }
    if (appInfo.versionCode <= currentVersionCode) {
        showNotificationDialog = TopPopUp(message = "You're up to date", icon = Icons.Default.Android)
        onDismissRequest()
    } else {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Update Available: v${appInfo.versionName}") },
            text = { Text("Changelog:\n${appInfo.whatsNew}") },
            confirmButton = {
                TextButton(onClick = {
                    DownloaderClass(context).downloadNewVersionAPK(appInfo)
                    onDismissRequest()
                }) { Text("Download") }
            },
            dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel") } }
        )
    }
}
