package com.das.forui.ui.watch_later

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das.forui.CustomTheme
import com.das.forui.MainActivity
import com.das.forui.MainApplication
import com.das.forui.R
import com.das.forui.databased.DatabaseFavorite
import com.das.forui.databased.SearchHistoryDB
import com.das.forui.databinding.FragmentWatchLaterBinding
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.SavedVideosListData
import com.das.forui.objectsAndData.VideosListData
import com.das.forui.services.AudioServiceFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchLaterFragment: Fragment() {
    private var _binding: FragmentWatchLaterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchLaterBinding.inflate(inflater, container, false)
        binding.root.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CustomTheme {
                    ListFavVideos()
                }
            }
        }


        return binding.root
    }







    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListFavVideos() {

        val searchResults = remember { mutableStateOf<List<SavedVideosListData>>(emptyList()) }
        val isLoading = remember { mutableStateOf(true) }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        LaunchedEffect(Unit) {
            isLoading.value = true
            val result = withContext(Dispatchers.IO){
                fetchDataFromDatabase()
            }
            searchResults.value = result ?: emptyList()
            isLoading.value = false
        }

        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    actions = {
                        Text(
                            "List of videos",
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterVertically)
                                .padding(start = 15.dp, end = 5.dp, bottom = 1.dp, top = 1.dp)
                        )
                    },
                    title = {}
                )
            }
        ){ paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }else {
                    if (searchResults.value.isEmpty()) {

                        Text(
                            text = "You haven't saved any videos yet. Save some videos to create your collection!",
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        LazyColumn {
                            items(searchResults.value) { searchItem ->
                                CategoryItems(searchItem) { videoId ->
                                    SearchHistoryDB(requireContext()).deleteSearchList(videoId)
                                    searchResults.value =
                                        searchResults.value.filter { it != searchItem }

                                }

                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun CategoryItems(
        selectedItem: SavedVideosListData,
        onDelete: (videoId: String) -> Unit
    ){
        val videoId  = selectedItem.watchUrl
        val title  = selectedItem.title
        val viewsNumber  = selectedItem.viewer
        val dateOfVideo  = selectedItem.dateTime
        val channelName  = selectedItem.channelName
        val duration  = selectedItem.duration
        val videoThumbnailURL  = selectedItem.thumbnailUrl
        val channelThumbnails  = selectedItem.channelName

        val context= LocalContext.current




        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(1))
                .padding(bottom = 3.dp, top = 3.dp)
                .combinedClickable(
                    onClick = {
                        onClickListListener(videoId)
                    },
                    onLongClick = {

                        AlertDialog.Builder(requireContext())
                            .setTitle("Are you sure you want to remove it from the list?")
                            .setPositiveButton("Yes") { _, _ ->
                                onDelete(title)
                            }
                            .setNegativeButton("No") { _, _ ->
                            }
                            .show()

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
                            .data(videoThumbnailURL)
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
                        textAlign = TextAlign.Justify,
                        modifier = Modifier
                            .height(25.dp)
                            .padding(end = 6.dp, bottom = 3.dp)
                            .align(Alignment.BottomEnd)
                            .background(Color(0xCC2C2B2B), RoundedCornerShape(25))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {


                    IconButton(
                        onClick = {
                            AlertDialog.Builder(context)
                                .setTitle("This feature is currently under development!!!")
                                .setPositiveButton("Okay") { _, _ -> }
                                .setIcon(R.drawable.setting)
                                .setMessage("Thank you for understanding!")
                                .setIcon(R.mipmap.ic_launcher)
                                .show()
                        }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(channelThumbnails)
                                .crossfade(true)
                                .error(
                                    R.mipmap.ic_launcher
                                )
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
                        Row {
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
                                selectedItem
                            ) { selectedId ->
                                onDelete(selectedId)
                            }
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


    private fun onClickListListener(selectedId: String){
        try {
            val dbHelper = DatabaseFavorite(requireContext())
            val viewNumber = dbHelper.getViewNumber(selectedId)
            val datVideo = dbHelper.getVideoDate(selectedId)
            val videoChannel = dbHelper.getVideoChannelName(selectedId)
            val ourDuration= dbHelper.getDuration(selectedId).toString()
            val title= dbHelper.getVideoTitle(selectedId)
            val bundle = Bundle().apply {
                putString("View_ID", selectedId)
                putString("View_URL", "https://www.youtube.com/watch?v=$selectedId")
                putString("View_Title", title)
                putString("View_Number", viewNumber)
                putString("dateOfVideo", datVideo)
                putString("channelName", videoChannel)
                putString("duration", ourDuration)
            }
            findNavController().navigate(R.id.nav_video_viewer, bundle)
//            Toast.makeText(context, selectedItem, Toast.LENGTH_SHORT).show()
//            (activity as MainActivity).playMedia(requireContext(), selectedId)
        } catch (e: Exception) {
            (activity as MainActivity).alertUserError(e.message.toString())
        }
    }



    private fun imageViewer(selectedData: SavedVideosListData, deleteTheItem: (selectedId: String) -> Unit) {


        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to remove this item?")
            .setPositiveButton("Yes") { _, _ ->
                deleteTheItem(selectedData.watchUrl)
            }
            .setNegativeButton("No") { _, _ -> }
            .setNeutralButton(
                "Background"
            ) { _, _ ->
                MainApplication().getListItemsStreamUrls(
                    VideosListData(
                        selectedData.watchUrl, selectedData.title, selectedData.viewer,
                        selectedData.dateTime, selectedData.duration, selectedData.channelName, ""
                    ),
                    onSuccess = { result ->
                        val playIntent =
                            Intent(
                                requireContext(),
                                AudioServiceFromUrl::class.java
                            ).apply {
                                action = ACTION_START
                                putExtra("videoId", selectedData.watchUrl)
                                putExtra("media_url", result.audioUrl)
                                putExtra("title", selectedData.title)
                                putExtra("channelName", selectedData.channelName)
                                putExtra("viewNumber", selectedData.viewer)
                                putExtra("videoDate", selectedData.dateTime)
                                putExtra("duration", selectedData.duration)
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

    private fun fetchDataFromDatabase(): MutableList<SavedVideosListData>? {
        val dbHelper = DatabaseFavorite(requireContext())
        val cursor: Cursor? = dbHelper.getResults()

        val savedVideosListData= mutableListOf<SavedVideosListData>()
        try {
            cursor?.let {
                while (it.moveToNext()) {
                    val watchUrl = it.getString(it.getColumnIndexOrThrow("video_id"))
                    val title = dbHelper.getVideoTitle(watchUrl).toString()
                    val viewerNumber = it.getString(it.getColumnIndexOrThrow("viewNumber"))
                    val dateTime = it.getString(it.getColumnIndexOrThrow("videoDate"))
                    val channelName = it.getString(it.getColumnIndexOrThrow("videoChannelName"))
                    val myDuration = it.getString(it.getColumnIndexOrThrow("duration"))
                    savedVideosListData.add(
                        SavedVideosListData(
                            title,
                            watchUrl,
                            "https://img.youtube.com/vi/$watchUrl/0.jpg",
                            viewerNumber,
                            dateTime,
                            myDuration,
                            channelName
                    )
                    )
                }
                it.close()
            }

            return savedVideosListData
        }catch (e:Exception){
            (activity as MainActivity).alertUserError(e.message.toString())
            return null
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}