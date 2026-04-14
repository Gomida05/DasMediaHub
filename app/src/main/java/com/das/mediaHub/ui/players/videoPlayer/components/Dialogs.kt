package com.das.mediaHub.ui.players.videoPlayer.components

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.das.downloader.data.model.download.DownloadType
import com.das.mediaHub.data.model.state.VideoUiState
import com.das.mediaHub.services.media.OnlineBackgroundPlayer.Companion.playAudioFromUrl
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.openCustomTab
import com.das.mediaHub.ui.theme.AppTheme
import com.das.mediaHub.ui.theme.ThemePreferences.loadDarkModeState
import com.das.python.YouTuber.loadStreamUrl
import com.das.python.data.model.VideosListData
import kotlinx.coroutines.launch


@Composable
fun ShowDescriptionDialog(text: String, onDismissRequest: () -> Unit) {
    val themeState by loadDarkModeState()
    val isDarkTheme = when (themeState) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val linkColor = if (isDarkTheme) Color(0xFF64B5F6) else Color(0xFF1565C0)
    val context = LocalContext.current
    val urlPattern = """https?://\S+""".toRegex()
    val matches = urlPattern.findAll(text)
    val annotation = AnnotatedString.Builder(text)

    matches.forEach { match ->
        val url = match.value
        annotation.addLink(
            LinkAnnotation.Clickable(
                tag = url,
                styles = TextLinkStyles(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
                linkInteractionListener = { context.openCustomTab(url.toUri()) }
            ),
            match.range.first, match.range.last + 1
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Description", fontWeight = FontWeight.Bold) },
        text = {
            SelectionContainer {
                Box(modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())) {
                    Text(
                        text = annotation.toAnnotatedString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismissRequest) { Text("Close") } }
    )
}

@Composable
fun ShowAlertDialog(selectedItem: VideosListData, onDismissRequest: (DownloadType?) -> Unit) {
    val mContext = LocalContext.current
    val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (isLoading.value) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching stream URL...")
                }
            }
        }
    }

    if (errorMessage.value != null) {
        AlertDialog(
            onDismissRequest = { errorMessage.value = null; onDismissRequest(null) },
            confirmButton = { TextButton(onClick = { errorMessage.value = null; onDismissRequest(null) }) { Text("OK") } },
            title = { Text("Error") },
            text = { Text(errorMessage.value!!) }
        )
    }

    if (!isLoading.value && errorMessage.value == null) {
        Dialog(onDismissRequest = { onDismissRequest(null) }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose Action",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                isLoading.value = true
                                scope.launch {
                                    selectedItem.loadStreamUrl(
                                        onSuccess = {
                                            mContext.playAudioFromUrl(it.audioUrl, it)
                                            isLoading.value = false
                                            onDismissRequest(null)
                                        },
                                        onFailure = { err ->
                                            errorMessage.value = err.message
                                            isLoading.value = false
                                        }
                                    )
                                }
                            }
                        ) { Text("Background") }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onDismissRequest(DownloadType.MUSIC) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Music") }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onDismissRequest(DownloadType.VIDEO) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Video") }
                    }
                }
            }
        }
    }
}

@Composable
fun AskToPlay(
    showAlertDialog: Boolean,
    mContext: Context,
    url: String,
    id: String,
    video: VideoUiState,
    onDismissRequest: () -> Unit
) {
    if (showAlertDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(28.dp),
            title = { Text("Background Play") },
            text = { Text("Do you want to continue playing this media in the background?") },
            confirmButton = {
                TextButton(onClick = {
                    mContext.playAudioFromUrl(
                        id = id,
                        audioUrl = url,
                        video
                    )
                    onDismissRequest()
                }) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = onDismissRequest) { Text("No") } }
        )
    }
}