package com.das.mediaHub.ui.players.videoPlayer

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.das.mediaHub.MainApplication
import com.das.mediaHub.PIP
import com.das.mediaHub.PIP.HandlePip
import com.das.mediaHub.PIP.disablePipAndScreenLock
import com.das.mediaHub.PIP.findActivity
import com.das.mediaHub.PIP.rememberIsInPipMode
import com.das.mediaHub.PIP.rememberPipModifier
import com.das.mediaHub.data.constants.GlobalVideoList
import com.das.mediaHub.data.mediacontroller.online.VideoPlayerListener
import com.das.mediaHub.data.model.state.UiState
import com.das.mediaHub.ui.players.videoPlayer.components.AskToPlay
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.openCustomTab
import com.das.mediaHub.ui.players.videoPlayer.components.CustomMethods.rotateScreen
import com.das.mediaHub.ui.players.videoPlayer.components.FullscreenVideoContent
import com.das.mediaHub.ui.players.videoPlayer.components.StandardVideoContent
import com.das.mediaHub.ui.players.videoPlayer.components.VideoPlayerEffects

@Composable
fun OnlineVideoPlayerScreen(
    backStack: NavBackStack<NavKey>,
    videoID: String
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    var currentVideoId by rememberSaveable { mutableStateOf(videoID) }
    var isInFullScreen by rememberSaveable { mutableStateOf(false) }

    val isInPipMode = rememberIsInPipMode()
    val showAlertDialog = retain(currentVideoId) { mutableStateOf(false) }

    val viewModel = viewModel(modelClass = ViewerViewModel::class.java.kotlin)
    val videoState by viewModel.videoState.collectAsStateWithLifecycle()
    val suggestionsState by viewModel.suggestionsState.collectAsStateWithLifecycle()
    val videoUiState by viewModel.videoUiState.collectAsStateWithLifecycle()

    val videoUrl = (videoState as? UiState.Success<String>)?.data
    val app = context.applicationContext as MainApplication
    val listener = retain {
        VideoPlayerListener {
            GlobalVideoList.getVideoAt(0)?.id?.let { nextId ->
                currentVideoId = nextId
            }
        }
    }
    val videoPlayerManager = retain { app.videoPlayerMainApplication }


    val exoPlayer = retain { videoPlayerManager.player }

    VideoPlayerEffects(
        window = activity?.window,
        videoId = currentVideoId,
        videoUrl = videoUrl,
        videoPlayerManager = videoPlayerManager,
        exoPlayer = exoPlayer,
        onInitialize = viewModel::initialize,
        onDisposePlayer = {
            disablePipAndScreenLock(activity)
            videoPlayerManager.release()
        }
    )

    BackHandler(enabled = true) {
        if (isInFullScreen) {
            isInFullScreen = false
            activity?.rotateScreen(false)
        } else {
            backStack.removeLastOrNull()
        }
    }

    DisposableEffect(videoPlayerManager) {
        videoPlayerManager.addListener(listener)
        onDispose {
            videoPlayerManager.removeListener(listener)
        }
    }

    PIP.BindPip(activity = activity)
    HandlePip(activity = activity)

    val onToggleFullscreen: (Boolean) -> Unit = { enabled ->
        isInFullScreen = enabled
        activity?.rotateScreen(enabled)
    }

    val openInYoutube: () -> Unit = {
        videoPlayerManager.pause()
        val currentTimeSec = exoPlayer.currentPosition / 1000
        val youtubeUrl =
            "https://www.youtube.com/watch?v=$currentVideoId&t=${currentTimeSec}s".toUri()

        try {
            val intent = Intent(Intent.ACTION_VIEW, youtubeUrl).apply {
                setPackage("com.google.android.youtube")
            }
            activity?.startActivity(intent)
        } catch (_: Exception) {
            context.openCustomTab(youtubeUrl)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { paddings ->
        if (isInFullScreen || isInPipMode) {
            FullscreenVideoContent(
                modifier = Modifier
                    .fillMaxSize()
                    .rememberPipModifier(),
                videoState = videoState,
                exoPlayer = exoPlayer,
                isInFullScreen = isInFullScreen,
                isInPipMode = isInPipMode,
                onToggleFullscreen = onToggleFullscreen
            )
        } else {
            StandardVideoContent(
                paddings = paddings,
                currentVideoId = currentVideoId,
                videoState = videoState,
                exoPlayer = exoPlayer,
                isInFullScreen = isInFullScreen,
                videoUiState = videoUiState,
                suggestionsState = suggestionsState,
                viewModel = viewModel,
                context = context,
                onToggleFullscreen = onToggleFullscreen,
                onOpenDetailsDialog = { showAlertDialog.value = true },
                onOpenInYoutube = openInYoutube,
                onSelectVideo = { selectedId -> currentVideoId = selectedId }
            )
        }

        AskToPlay(
            showAlertDialog = showAlertDialog.value,
            mContext = context,
            url = videoUrl.orEmpty(),
            id = currentVideoId,
            video = videoUiState,
            onDismissRequest = { showAlertDialog.value = false }
        )
    }
}
