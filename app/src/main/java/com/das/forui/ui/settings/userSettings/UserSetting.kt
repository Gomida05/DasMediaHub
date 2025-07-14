package com.das.forui.ui.settings.userSettings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import com.das.forui.Screen
import com.das.forui.data.databased.PathSaver.setAudioDownloadPath
import com.das.forui.data.databased.PathSaver.setMoviesDownloadPath
import com.das.forui.theme.ThemePreferences.loadDarkModeState
import com.das.forui.theme.ThemePreferences.saveDarkMode
import com.das.forui.theme.AppTheme


@Composable
fun UserSettingComposable(navController: NavController) {
    val context = LocalContext.current

    val snackBarHostState = remember { SnackbarHostState() }

    var showSnackBar by remember { mutableStateOf(false) }

    var showAlertDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showSnackBar) {
        if (showSnackBar) {
            snackBarHostState.showSnackbar("Currently under development")
            showSnackBar = false
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                SettingCard(title = "Appearance") {
                    DarkModeToggleWithPrefs(context)
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SettingCard(title = "New Features") {
                    TestLoginPage1 {
                        navController.navigate(Screen.LoginPage1.route)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SettingCard(title = "Settings") {
                    SecuritySettings {
                        showSnackBar = true
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                SettingCard(title = "Settings") {

                    Change_Downloading_Location {
                        showAlertDialog = true
                    }
                }
            }
        }
    }
    FolderPickerDialog(context, showAlertDialog, onDismiss = { showAlertDialog = false })

}


@Composable
fun SecuritySettings(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Security, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text("Security Settings")
    }
}

@Composable
private fun TestLoginPage1(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text("Test Login page 1")
    }

}

@Composable
private fun Change_Downloading_Location(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Folder, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text("Change Downloading Location")
    }

}

@Composable
fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}



@Composable
fun DarkModeToggleWithPrefs(context: Context) {
    val themeState by loadDarkModeState(context)
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (themeState) {
                AppTheme.LIGHT -> Icons.Default.LightMode
                AppTheme.DARK -> Icons.Default.DarkMode
                else -> Icons.Default.Contrast
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Theme: ${themeState.name}",
            style = MaterialTheme.typography.bodyLarge
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
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
                        Text(theme.name)
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




@Composable
fun FolderPickerDialog(context: Context, showDialog: Boolean, onDismiss: () -> Unit) {

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                getFolderPathFromUri(context, it, "audio")
            }
        }
    )

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            uri?.let {
                getFolderPathFromUri(context, it, "video")
            }
        }
    )

    if (showDialog) {

        AlertDialogPathChoose(
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
fun AlertDialogPathChoose(
    onDismissRequest: () -> Unit = {},
    onAudioSelect: () -> Unit,
    onVideoSelect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Which location do you want to change? Please select one of them:")
        },
        confirmButton = {
            TextButton(onClick = {
                onAudioSelect()
                onDismissRequest()
            }) {
                Text("Audio's")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onVideoSelect()
                onDismissRequest()
            }) {
                Text("Video's")
            }
        }
    )
}






private fun extractFolderPath(path: String): String {
    val prefix = "/tree/primary:"
    return if (path.startsWith(prefix)) {
        path.removePrefix(prefix)
    } else {
        path
    }
}



private fun getFolderPathFromUri(mContext: Context, uri: Uri, type: String): String? {
    val path = uri.path

    try {

        val documentFile = DocumentFile.fromTreeUri(mContext, uri)


        if (documentFile != null && documentFile.isDirectory) {

            val pather = "/storage/emulated/0/${extractFolderPath(path.toString())}"
            if (type == "video") {

                setMoviesDownloadPath(mContext, pather)

            } else if (type == "audio") {

                setAudioDownloadPath(mContext, pather)

            }
        } else {
            println("URI is not a directory or invalid")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error: ${e.message}")
    }

    return path
}