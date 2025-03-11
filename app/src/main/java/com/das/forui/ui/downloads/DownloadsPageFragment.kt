package com.das.forui.ui.downloads

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.das.forui.MainActivity
import com.das.forui.MainActivity.Youtuber.PLAY_HERE_VIDEO
import com.das.forui.services.MyService
import com.das.forui.R
import com.das.forui.databased.PathSaver
import com.das.forui.databinding.FragmentDownloadsBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadsPageFragment: Fragment() {
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DownloadedArrayAdapter
    private val ids = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        adapter = DownloadedArrayAdapter(requireContext(), mutableListOf())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideBottomNav()

        binding.downloadedHistory.adapter = adapter

        fetchDataFromDatabase()


    }

    override fun onStart() {
        super.onStart()
        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.button2.setOnClickListener {
            (activity as MainActivity).startBanner(false)
            val intent = Intent(requireContext(), MyService::class.java)
            requireContext().stopService(intent)

        }

        binding.downloadedHistory.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position) // Get the item clicked in the list

            val filePath = selectedItem?.pathOfVideo

            println("here is the file name: $filePath")

            val bundle = Bundle().apply {
                putString(PLAY_HERE_VIDEO, filePath.toString())
            }
            findNavController().navigate(
                R.id.nav_fullscreen,
                bundle

            )

        }

        binding.downloadedHistory.setOnItemLongClickListener {_, _, position, _ ->
            val selectedItem = adapter.getItem(position)

            AlertDialog.Builder(requireContext())
                .setTitle("Are you sure you want to delete this file?")
                .setPositiveButton("Yes") { _, _ ->
                    adapter.removeItem(position, selectedItem?.pathOfVideo!!)
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
            true
        }


        activity?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
               findNavController().navigateUp()
            }
        }
        )
    }



    private fun fetchDataFromDatabase() {

        val downloadedListData = mutableListOf<DownloadedListData>()
        val urls = mutableListOf<String>()

        ids.clear()
        val pathOfVideos = File(PathSaver().getVideosDownloadPath(requireContext())!!)
        if (pathOfVideos.exists()) {
            val fileNames = arrayOfNulls<String>(pathOfVideos.listFiles()!!.size)
            val pathOfVideosUris = arrayOfNulls<Uri?>(pathOfVideos.listFiles()!!.size)
            pathOfVideos.listFiles()!!.mapIndexed { index, item ->
                fileNames[index] = item?.name
                pathOfVideosUris[index] = item?.toUri()

            }
            fileNames.zip(pathOfVideosUris).forEach { (fileName, videoUri) ->
                if (videoUri != null && fileName != null) {
                    val videoFile = File(pathOfVideos, fileName)
                    val lastModified = videoFile.lastModified()
                    val formattedDate = formatDate(lastModified)
                    val fileSizeFormatted = formatFileSize(videoFile.length())
                    downloadedListData.add(
                        DownloadedListData(
                            title = fileName,
                            pathOfVideo = videoUri,
                            thumbnailUri = videoUri,
                            dateTime = formattedDate,
                            fileSize = fileSizeFormatted
                        )
                    )
                    videoUri.let { url ->
                        urls.add(fileName)
                        ids.add(url)
                    }
                }
            }
            if (downloadedListData.isNotEmpty()) {
                adapter.clear()
                adapter.addAll(downloadedListData)
                adapter.notifyDataSetChanged()
            } else {
                Log.e("DownloadsFragment", "URLs list is empty or null")
            }
        }

    }


    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }


    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes >= 1_073_741_824 -> String.format(Locale.ROOT, "%.2f GB", sizeInBytes / 1_073_741_824.0)
            sizeInBytes >= 1_048_576 -> String.format(Locale.ROOT, "%.2f MB", sizeInBytes / 1_048_576.0)
            sizeInBytes >= 1_024 -> String.format(Locale.ROOT, "%.2f KB", sizeInBytes / 1_024.0)
            else -> String.format(Locale.ROOT, "%d bytes", sizeInBytes)
        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    private data class DownloadedListData(
        val title: String,
        val pathOfVideo: Uri,
        val thumbnailUri: Uri,
        val dateTime: String,
        val fileSize: String
    )

    private class DownloadedArrayAdapter(
        private val context: Context,
        private val downloadedLists: MutableList<DownloadedListData>
    ): ArrayAdapter<DownloadedListData>(
        context, R.layout.watch_later_list, downloadedLists
    ){

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.downloaded_list_item, parent, false)

            val downloadedLists = getItem(position) ?: return view
            val titleTextView: TextView = view.findViewById(R.id.give_me_title_for_this_video)
            val thumbnailImageView: ImageView = view.findViewById(R.id.give_me_thumbnail_here)
            val dateShow: TextView = view.findViewById(R.id.give_me_date_for_this_video)
            val giveFileSize: TextView = view.findViewById(R.id.give_me_size_for_this_video)
//            val duration: TextView = view.findViewById(R.id.Watch_video_list_duration)



            titleTextView.text = downloadedLists.title
            dateShow.text= downloadedLists.dateTime
            giveFileSize.text = downloadedLists.fileSize
//            duration.text= downloadedLists.duration


            Glide.with(context)
                .load(downloadedLists.thumbnailUri)
                .placeholder(R.drawable.music_note_24dp)
                .centerCrop()
                .into(thumbnailImageView)
            return view
        }

        fun removeItem(position: Int, videoId: Uri) {
            File(videoId.path!!).delete()
            downloadedLists.removeAt(position)
            notifyDataSetChanged()
        }
    }
}
