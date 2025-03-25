package com.das.forui.ui.downloads

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databased.PathSaver
import com.das.forui.databinding.FragmentDownloadsBinding
import com.das.forui.objectsAndData.DownloadedListData
import com.das.forui.objectsAndData.ForUIKeyWords.ACTION_START
import com.das.forui.objectsAndData.ForUIKeyWords.MEDIA_TITLE
import com.das.forui.objectsAndData.ForUIKeyWords.PLAY_HERE_VIDEO
import com.das.forui.services.BackGroundPlayer
import java.io.File


class DownloadsPageFragment: Fragment() {
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DownloadedArrayAdapter
    private var typeFile: Int = 1

    private val downloadsPageViewModel: DownloadsPageViewModel by viewModels()

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

        downloadsPageViewModel.downloadedListData.observe(viewLifecycleOwner) { data ->
            adapter.clear()
            adapter.addAll(data)
            adapter.notifyDataSetChanged()
        }

        binding.downloadedHistory.adapter = adapter

        val videoPath = PathSaver().getVideosDownloadPath(requireContext())
        downloadsPageViewModel.fetchDataFromDatabase(videoPath, 1)


    }

    override fun onStart() {
        super.onStart()


        val videoPath = PathSaver().getVideosDownloadPath(requireContext())
        val audioPath = PathSaver().getAudioDownloadPath(requireContext())

        binding.listVideos.setOnClickListener {
            binding.listVideos.isEnabled = false
            binding.listAudios.isEnabled = true
            adapter.clear()
            downloadsPageViewModel.fetchDataFromDatabase(videoPath, 1)
            typeFile = 1
        }


        binding.listAudios.setOnClickListener {
            binding.listAudios.isEnabled = false
            binding.listVideos.isEnabled = true
            adapter.clear()
            downloadsPageViewModel.fetchDataFromDatabase(audioPath, 0)
            typeFile = 0
        }

        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.downloadedSettings.setOnClickListener {
            (activity as MainActivity).showDialogs("Coming soon!")
        }

        binding.downloadedHistory.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position) // Get the item clicked in the list

            val filePath = selectedItem?.pathOfVideo
            val title = selectedItem?.title

            println("here is the file name: $filePath")
            if (typeFile == 1) {
                val bundle = Bundle().apply {
                    putString(PLAY_HERE_VIDEO, filePath.toString())
                    putString(MEDIA_TITLE, title)
                }
                findNavController().navigate(
                    R.id.nav_fullscreen,
                    bundle

                )
            }else {

                val playIntent = Intent(requireContext(), BackGroundPlayer::class.java).apply {
                    action = ACTION_START
                    putExtra("media_id", filePath.toString())
                    putExtra("media_url", filePath.toString())
                    putExtra("title", title)
                }
                activity?.startService(playIntent)

            }


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
    }







    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }



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
                .placeholder(
                    if (downloadedLists.type == 1) R.drawable.smart_display_24dp
                    else
                    R.drawable.music_note_24dp
                )
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
