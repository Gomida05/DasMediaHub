package com.das.forui.ui.result

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.das.forui.MainActivity
import com.das.forui.databinding.FragmentResultBinding
import com.das.forui.MainActivity.Youtuber.pythonInstant
import com.das.forui.MainApplication
import com.das.forui.R
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.services.AudioServiceFromUrl
import com.das.forui.ui.viewer.ViewerFragment.Video
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)

        binding.myComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val text = arguments?.getString("EXTRA_TEXT").toString()
                ResultViewerPage(text)
            }
        }
        return binding.root
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ResultViewerPage(data: String?) {
        val searchResults = remember { mutableStateOf<List<SearchResultFromMain>>(emptyList()) }
        val isLoading = remember { mutableStateOf(true) }
        LaunchedEffect(data) {
            if (!data.isNullOrEmpty()) {
                isLoading.value = true

                val result = withContext(Dispatchers.IO) {
                    callPythonForSearchVideos(data)
                }
                searchResults.value = result ?: emptyList()
                isLoading.value = false
            }
        }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                    },
                    actions = {
                        Button(onClick = {
                            findNavController().navigateUp()
                                         },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end=12.dp, bottom = 1.dp, top = 1.dp)
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Outlined.Search),
                                contentDescription = "Back"
                            )
                            Text("$data")
                        }
                    }
                )
            }
        )
        { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    if (searchResults.value.isEmpty()) {
                        Text(
                            text = "No results found",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn(modifier = Modifier) {
                            items(searchResults.value) { searchItem ->
                                CategoryItems(
                                    videoId = searchItem.videoId,
                                    title = searchItem.title,
                                    viewsNumber = searchItem.views,
                                    dateOfVideo = searchItem.dateOfVideo,
                                    channelName = searchItem.channelName,
                                    duration = searchItem.duration,
                                    videoThumbnailURL = searchItem.videoId,
                                    channelThumbnails = searchItem.channelThumbnailsUrl,

                                    )
                            }
                        }
                    }
                }

            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun CategoryItems(
        videoId: String,
        title: String,
        viewsNumber: String,
        dateOfVideo: String,
        channelName: String,
        duration: String,
        videoThumbnailURL: String,
        channelThumbnails: String
    ){
        val context= requireContext()

        Card(
            shape = RoundedCornerShape(1),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp, top = 3.dp)
                .combinedClickable (
                    onClick = {
                        val bundle = Bundle().apply {
                            putString("View_ID", videoId)
                            putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                            putString("View_Title", title)
                            putString("View_Number", viewsNumber)
                            putString("dateOfVideo", dateOfVideo)
                            putString("channelName", channelName)
                            putString("duration", duration)
                            putString("channel_Thumbnails", channelThumbnails)
                        }
                        findNavController().navigate(R.id.nav_video_viewer, bundle)
                    },
                    onLongClick = {
                        imageViewer(
                            Video(
                                videoId, title, viewsNumber, dateOfVideo,
                                duration, channelName, channelThumbnails
                            )
                        )
                    }
                )
        ) {
            Column (
                modifier = Modifier
                    .height(260.dp)
                    .fillMaxWidth()

            ) {
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("https://img.youtube.com/vi/$videoThumbnailURL/0.jpg")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Category Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        alignment = Alignment.Center,
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = duration,
                        maxLines = 1,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(40.dp)
                            .padding(end = 3.dp, bottom = 3.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color.DarkGray, RoundedCornerShape(5.dp))
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {


                    IconButton(
                        onClick = {
                            Toast.makeText(context, channelName, Toast.LENGTH_SHORT).show() }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(channelThumbnails)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Category Image",
                            modifier = Modifier
                                .fillMaxSize(),
                            alignment = Alignment.Center,
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(
                        modifier = Modifier
                            .width(285.dp)
                            .padding(3.dp)
                    ) {


                        Text(
                            text = title,
                            maxLines = 1,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 2.dp)
                        )
                        Row{
                            Text(
                                text = channelName,
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .width(112.dp)
                                    .padding(start = 2.dp)
                            )
                            Text(
                                text = viewsNumber,
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(55.dp)
                                    .padding(start = 5.dp, end = 5.dp)
                            )
                            Text(
                                text = dateOfVideo,
                                maxLines = 1,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .width(100.dp)
                                    .padding(start = 2.dp)
                            )
                        }

                    }
                        IconButton(
                            onClick = {
                             imageViewer(
                                 Video(
                                     videoId, title, viewsNumber, dateOfVideo,
                                     duration, channelName, channelThumbnails
                                 )
                             )
                            }

                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.MoreVert),
                                contentDescription = "Back"
                            )
                        }
                }
            }
        }
    }



    private fun callPythonForSearchVideos(inputText: String): List<SearchResultFromMain>? {
        return try {
            val mainFile = pythonInstant.getModule("main")
            val variable = mainFile["Searcher"]?.call(inputText).toString()
            val videoListType = object : TypeToken<List<SearchResultFromMain>>() {}.type
            val videoList: List<SearchResultFromMain> = Gson().fromJson(variable, videoListType)
            videoList
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun imageViewer(selectedItem: Video) {
        val thumbnailUrl = "https://img.youtube.com/vi/${selectedItem.videoId}/0.jpg"
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_with_image, null)
        val imageView: ImageView = dialogView.findViewById(R.id.dialog_image)

        Glide.with(this)
            .load(thumbnailUrl)
            .error(R.drawable.close)
            .centerCrop()
            .into(imageView)
        AlertDialog.Builder(context)
            .setTitle("Do you want to download it as video or audio?")
            .setView(dialogView)
            .setPositiveButton("Video") { _, _ ->
                (activity as MainActivity).downloadVideo(
                    selectedItem.videoId,
                    selectedItem.title,
                    requireContext()
                )
            }
            .setNegativeButton("Music") { _, _ ->
                (activity as MainActivity).downloadMusic(
                    selectedItem.videoId,
                    selectedItem.title,
                    requireContext()
                )
            }
            .setNeutralButton(
                "Background"
            ) { _, _, ->
                MainApplication().getListItemsStreamUrls(
                    selectedItem,
                    onSuccess = { result ->
                        val playIntent =
                            Intent(requireContext(), AudioServiceFromUrl::class.java).apply {
                                action = ACTION_START
                                putExtra("videoId", selectedItem.videoId)
                                putExtra("media_url", result.audioUrl)
                                putExtra("title", selectedItem.title)
                                putExtra("channelName", selectedItem.channelName)
                                putExtra("viewNumber", selectedItem.views)
                                putExtra("videoDate", selectedItem.dateOfVideo)
                                putExtra("duration", selectedItem.duration)
                            }
                        activity?.startService(playIntent)
                    },
                    onFailure = { errorMessage ->
                        // Handle the error (e.g., show a dialog with the error message)
                        println("Error: $errorMessage")
                    }
                )
            }.show()
    }



    data class SearchResultFromMain(
        val videoId: String,
        val title: String,
        val views: String,
        val dateOfVideo: String,
        val duration: String,
        val channelName: String,
        val channelThumbnailsUrl: String
    )

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNav()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
