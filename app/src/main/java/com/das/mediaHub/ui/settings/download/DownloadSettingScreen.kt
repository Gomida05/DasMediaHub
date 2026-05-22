package com.das.mediaHub.ui.settings.download

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.das.downloader.data.model.PathType
import com.das.mediaHub.data.local.ThemePreferences
import com.das.mediaHub.ui.settings.aboutBackgroundBrush

@Composable
fun DownloadSettingScreen(
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<DownloadSettingViewModel>()

    val snackBarHostState = remember { SnackbarHostState() }

    // Collect states
    val uiState by viewModel.uiState.collectAsState()
    val audioPath by ThemePreferences.audioPathState()
    val videoPath by ThemePreferences.videoPathState()

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
            .apply {
                createIntent(
                    context,
                    audioPath.toUri()
                )
            }
    ) { uri ->
        viewModel.onFolderPicked(pathType = PathType.AUDIO, uri = uri)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
            .apply {
                createIntent(
                    context,
                    audioPath.toUri()
                )
            }
    ) { uri ->
        viewModel.onFolderPicked(pathType = PathType.VIDEO, uri = uri)
    }

    // Handle One-Off Effects (Navigation, Toasts, Launchers)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DownloadSettingEffect.PickFolder -> {
                    when (effect.pathType) {
                        PathType.AUDIO -> audioPickerLauncher.launch(null)
                        PathType.VIDEO -> videoPickerLauncher.launch(null)
                    }
                }
                is DownloadSettingEffect.ShowMessage -> {
                    snackBarHostState.showSnackbar(
                        effect.message
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeContent,
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(aboutBackgroundBrush())
        ) {
            item {
                SettingCategoryTitle("Network")
            }

            item {
                SwitchSettingItem(
                    title = "Download over mobile data",
                    subtitle = "Allow downloading when not connected to Wi-Fi",
                    checked = uiState.downloadOverData,
                    onCheckedChange = { viewModel.updateDownloadOverData(it) }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                SettingCategoryTitle("Limits")
            }

            item {
                SliderSettingItem(
                    title = "Max concurrent downloads",
                    subtitle = "${uiState.maxConcurrentDownloads} items",
                    value = uiState.maxConcurrentDownloads.toFloat(),
                    valueRange = 1f..5f, // Allows 1 to 5 simultaneous downloads
                    onValueChange = { viewModel.updateMaxConcurrentDownloads(it.toInt()) }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                SettingCategoryTitle("Storage")
            }

            item {
                ClickableSettingItem(
                    title = "Audio Download Location",
                    subtitle = audioPath.formatPathForDisplay(),
                    onClick = { viewModel.onPickFolderRequested(PathType.AUDIO) }
                )
            }

            item {
                ClickableSettingItem(
                    title = "Video Download Location",
                    subtitle = videoPath.formatPathForDisplay(),
                    onClick = { viewModel.onPickFolderRequested(PathType.VIDEO) }
                )
            }
        }
    }
}

// --- Reusable UI Components for Settings ---

@Composable
fun SettingCategoryTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SliderSettingItem(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = (valueRange.endInclusive - valueRange.start).toInt() - 1
        )
    }
}

@Composable
fun ClickableSettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun String.formatPathForDisplay(): String {
    if (this == "Not set") return "Not set yet"

    return replace("/storage/emulated/0/", "Internal storage/")
        .ifBlank { "Internal storage" }
}