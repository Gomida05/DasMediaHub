package com.das.mediaHub.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.pm.PackageInfoCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.das.mediaHub.data.local.PathPreferences
import com.das.mediaHub.data.local.PathPreferences.saveAudioPath
import com.das.mediaHub.data.local.PathPreferences.saveVideoPath
import com.das.mediaHub.data.model.AppUpdateInfo
import com.das.mediaHub.data.model.TopPopUp
import com.das.mediaHub.navigation.NavScreens
import com.das.mediaHub.services.download.DownloadService
import com.das.mediaHub.ui.TopPopupNotification.showNotificationDialog
import com.das.mediaHub.ui.players.videoPlayer.state.UiState
import com.das.mediaHub.ui.theme.AppTheme
import com.das.mediaHub.ui.theme.ThemePreferences.loadDarkModeState
import com.das.mediaHub.ui.theme.ThemePreferences.saveDarkMode
import kotlinx.coroutines.delay

@Composable
fun SettingsComposable(add: (NavScreens) -> Unit) {
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val viewModel = viewModel(modelClass = SettingsViewModel::class.java)

    val packageInfo = remember {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }

    val showDialog = remember { mutableStateOf(false) }

    val uiState by viewModel.apkInfoState.collectAsStateWithLifecycle()

    var appearanceExpanded by rememberSaveable { mutableStateOf(false) }

    var storageExpanded by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }

    var audioPath by rememberSaveable { mutableStateOf("Not set") }
    var videoPath by rememberSaveable { mutableStateOf("Not set") }

    LaunchedEffect(Unit) {
        audioPath = PathPreferences.getAudioPath(context)
        videoPath = PathPreferences.getVideoPath(context)
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

                item {
                    SettingsSection(title = "Personalization") {
                        ExpandableSettingsItem(
                            icon = Icons.Default.ColorLens,
                            text = "Appearance",
                            subtitle = "Theme and display preferences",
                            expanded = appearanceExpanded,
                            onClick = { appearanceExpanded = !appearanceExpanded }
                        )

                        AnimatedVisibility(
                            visible = appearanceExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                )

                                InlineThemeSelector(context = context)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        )

                        ExpandableSettingsItem(
                            icon = Icons.Default.Folder,
                            text = "Storage",
                            subtitle = "Manage audio and video download folders",
                            expanded = storageExpanded,
                            onClick = { storageExpanded = !storageExpanded }
                        )

                        AnimatedVisibility(
                            visible = storageExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                )

                                SettingsSubItem(
                                    icon = Icons.Default.Folder,
                                    title = "Download Location",
                                    subtitle = buildDownloadLocationSubtitle(
                                        audioPath = audioPath,
                                        videoPath = videoPath
                                    ),
                                    onClick = { showFolderDialog = true }
                                )
                            }
                        }
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

        when (val newState = uiState) {
            is UiState.Error -> {
                ErrorDialog(
                    message = newState.message,
                    onDismiss = { viewModel.clearResult() }
                ) {
                    viewModel.retryLoad()
                }
            }
            is UiState.Success -> {
                ShowAlertDialog(
                    context = context,
                    appInfo = newState.data,
                    packageInfo = packageInfo,
                    onDismissRequest = {
                        showDialog.value = false
                        viewModel.clearResult()
                    }
                )
            }

            UiState.Loading -> {
                LoadingDialog { viewModel.cancelLoading() }
            }
            else -> Unit
        }

        FolderPickerDialog(
            showDialog = showFolderDialog,
            onDismiss = { showFolderDialog = false },
            onPathSaved = { type, savedPath ->
                showFolderDialog = false

                when (type.lowercase()) {
                    "audio" -> audioPath = savedPath
                    "video" -> videoPath = savedPath
                }
            }
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
fun LoadingDialog(
    title: String = "Checking for updates",
    onCancel: () -> Unit
) {
    val animatedMessage = loadingMessage()
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 3.5.dp,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))




                Text(
                    text = animatedMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}



@Composable
private fun loadingMessage(): String {
    var dots by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            dots = when (dots) {
                "" -> "."
                "." -> ".."
                ".." -> "..."
                else -> ""
            }
            delay(400)
        }
    }

    return "Fetching the latest update info...$dots"
}


@Composable
private fun StyledDialogContainer(
    icon: ImageVector? = null,
    title: String,
    message: String,
    actions: @Composable () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {

        }
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            }
        }
    }
}


@Composable
private fun ErrorDialog(
    message: String?,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    if (!message.isNullOrEmpty()) {
        StyledDialogContainer(
            icon = Icons.Default.ErrorOutline,
            title = "Something went wrong",
            message = message
        ) {
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
fun ShowAlertDialog(
    context: Context,
    appInfo: AppUpdateInfo,
    packageInfo: PackageInfo,
    onDismissRequest: () -> Unit
) {
    val currentVersionCode = remember { PackageInfoCompat.getLongVersionCode(packageInfo) }

    if (appInfo.versionCode <= currentVersionCode) {
        showNotificationDialog = TopPopUp(
            message = "You're up to date",
            icon = Icons.Default.Android
        )
        onDismissRequest()
    } else {
        StyledDialogContainer(
            icon = Icons.Default.SystemUpdateAlt,
            title = "Update Available",
            message = buildString {
                append("Version: ${appInfo.versionName}")
                if (appInfo.whatsNew.isNotBlank()) {
                    append("\n\nWhat’s new:\n${appInfo.whatsNew}")
                }
            }
        ) {
            TextButton(onClick = onDismissRequest) {
                Text("Later")
            }
            TextButton(
                onClick = {
                    DownloadService.startForApk(context, appInfo)
                    onDismissRequest()
                }
            ) {
                Text("Download")
            }
        }
    }
}


@Composable
fun ExpandableSettingsItem(
    icon: ImageVector,
    text: String,
    expanded: Boolean,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SettingsSubItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(start = 24.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InlineThemeSelector(context: Context) {
    val themeState by loadDarkModeState()
    var expanded by remember { mutableStateOf(false) }

    val currentIcon = when (themeState) {
        AppTheme.LIGHT -> Icons.Default.LightMode
        AppTheme.DARK -> Icons.Default.DarkMode
        AppTheme.SYSTEM -> Icons.Default.Contrast
    }

    Box {
        SettingsSubItem(
            icon = currentIcon,
            title = "Theme",
            subtitle = "Current: ${themeState.name.lowercase().replaceFirstChar { it.uppercase() }}",
            onClick = { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            AppTheme.entries.forEach { theme ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = themeState == theme,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = theme.name.lowercase().replaceFirstChar { it.uppercase() }
                            )
                        }
                    },
                    trailingIcon = {
                        if (themeState == theme) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        saveDarkMode(context, theme)
                        expanded = false
                    }
                )
            }
        }
    }
}



@Composable
fun FolderPickerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPathSaved: (String, String) -> Unit
) {
    val context = LocalContext.current.applicationContext

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                persistFolderPermission(context, it)
                val savedPath = getFolderPathFromUri(context, it, "audio")
                if (savedPath != null) {
                    onPathSaved("Audio", savedPath)
                }
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                persistFolderPermission(context, it)
                val savedPath = getFolderPathFromUri(context, it, "video")
                if (savedPath != null) {
                    onPathSaved("Video", savedPath)
                }
            }
        }
    )

    if (showDialog) {
        StorageTypeDialog(
            onDismissRequest = onDismiss,
            onAudioSelect = {
                audioPickerLauncher.launch(null)
            },
            onVideoSelect = {
                videoPickerLauncher.launch(null)
            }
        )
    }
}

@Composable
fun StorageTypeDialog(
    onDismissRequest: () -> Unit,
    onAudioSelect: () -> Unit,
    onVideoSelect: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Storage Location",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose which media type you want to change the download folder for.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            onAudioSelect()
                            onDismissRequest()
                        }
                    ) {
                        Text("Audio")
                    }
                    TextButton(
                        onClick = {
                            onVideoSelect()
                            onDismissRequest()
                        }
                    ) {
                        Text("Video")
                    }
                }
            }
        }
    }
}

private fun persistFolderPermission(context: Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    } catch (_: Exception) {
    }
}

private fun extractFolderPath(path: String): String {
    val prefix = "/tree/primary:"
    return path.removePrefix(prefix)
}

private fun getFolderPathFromUri(context: Context, uri: Uri, type: String): String? {
    return try {
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        if (documentFile != null && documentFile.isDirectory) {
            val folderPath = "/storage/emulated/0/${extractFolderPath(uri.path.toString())}"
            when (type) {
                "video" -> saveVideoPath(context = context, path = folderPath)
                "audio" -> saveAudioPath(context = context, path = folderPath)
            }
            folderPath
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun buildDownloadLocationSubtitle(
    audioPath: String,
    videoPath: String
): String {
    fun shortPath(path: String): String {
        if (path == "Not set") return path
        return path
            .removePrefix("/storage/emulated/0/")
            .ifBlank { "Internal storage" }
    }

    return "Audio: ${shortPath(audioPath)}\nVideo: ${shortPath(videoPath)}"
}