package com.das.forui.ui.watch_later

import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.das.forui.DatabaseFavorite
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databinding.FragmentWatchLaterBinding

class WatchLaterFragment: Fragment() {
    private var _binding: FragmentWatchLaterBinding? = null
    private val binding get() = _binding!!
    private lateinit var duration: String
    private lateinit var adapter: WatchLaterAdapter
    private val ids = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchLaterBinding.inflate(inflater, container, false)
        adapter = WatchLaterAdapter(requireContext(), mutableListOf())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.listView.adapter = adapter
        fetchDataFromDatabase()


    }

    override fun onStart() {
        super.onStart()

        binding.listView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(requireContext())
                .setTitle("Are you sure you want to remove this item?")
                .setPositiveButton("Yes") { _, _ ->
                    val selectedId = ids[position]
                    adapter.removeItem(position, selectedId)
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
            true
        }
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            onClickListListener(ids[position])
        }
    }


    private fun onClickListListener(selectedId: String){
        try {
            val dbHelper = DatabaseFavorite(requireContext())
            val viewNumber = dbHelper.getViewNumber(selectedId)
            val datVideo = dbHelper.getVideoDate(selectedId)
            val videoChannel = dbHelper.getVideoChannelName(selectedId)
            duration= dbHelper.getDuration(selectedId).toString()
            val title= dbHelper.getVideoTitle(selectedId)
            val bundle = Bundle().apply {
                putString("View_ID", selectedId)
                putString("View_URL", "https://www.youtube.com/watch?v=$selectedId")
                putString("View_Title", title)
                putString("View_Number", viewNumber)
                putString("dateOfVideo", datVideo)
                putString("channelName", videoChannel)
                putString("duration", duration)
            }
            findNavController().navigate(R.id.nav_video_viewer, bundle)
//            Toast.makeText(context, selectedItem, Toast.LENGTH_SHORT).show()
//            (activity as MainActivity).playMedia(requireContext(), selectedId)
        } catch (e: Exception) {
            (activity as MainActivity).alertUserError(e.message.toString())
        }
    }

    private fun fetchDataFromDatabase() {
        val dbHelper = DatabaseFavorite(requireContext())
        val cursor: Cursor? = dbHelper.getResults()
        if(!dbHelper.isTableEmpty()){
            binding.informUserWatchLater.visibility= View.GONE
        }
        else{
            binding.informUserWatchLater.visibility= View.VISIBLE
        }
        Log.d("WatchLater", "Query executed, cursor count: ${cursor?.count ?: "null"}")
        val urls = mutableListOf<String>()
        val watchLaterLists= mutableListOf<WatchLaterList>()
        ids.clear()
        try {
            cursor?.let {
                while (it.moveToNext()) {
                    val watchUrl = it.getString(it.getColumnIndexOrThrow("video_id"))
                    val title = dbHelper.getVideoTitle(watchUrl).toString()
                    val viewerNumber = it.getString(it.getColumnIndexOrThrow("viewNumber"))
                    val dateTime = it.getString(it.getColumnIndexOrThrow("videoDate"))
                    val channelName = it.getString(it.getColumnIndexOrThrow("videoChannelName"))
                    duration = it.getString(it.getColumnIndexOrThrow("duration"))
                    watchLaterLists.add(
                        WatchLaterList(
                            title,
                            "https://www.youtube.com/watch?v=$watchUrl",
                            "https://img.youtube.com/vi/$watchUrl/0.jpg",
                            viewerNumber,
                            dateTime,
                            duration,
                            channelName
                    )
                    )
                    watchUrl?.let { url ->
                        urls.add("$title ")
                        ids.add(url)
                    }
                }
                it.close()
            }

            if (watchLaterLists.isNotEmpty()) {
                adapter.clear()
                adapter.addAll(watchLaterLists)
                adapter.notifyDataSetChanged()
            } else {
//            Log.e("DownloadsFragment", "URLs list is empty or null")
            }
        }catch (e:Exception){
            (activity as MainActivity).alertUserError(e.message.toString())
        }
    }




    data class WatchLaterList(val title: String, val watchUrl: String, val thumbnailUrl: String, val viewer: String, val dateTime: String, val duration: String, val channelName: String)

    class WatchLaterAdapter(
        context: Context,
        private val watchLaterLists: MutableList<WatchLaterList>
    ) : ArrayAdapter<WatchLaterList>(context, R.layout.watch_later_list, watchLaterLists) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.watch_later_list, parent, false)

            val watchLaterLists = getItem(position) ?: return view
            val titleTextView: TextView = view.findViewById(R.id.Watch_video_list_name)
            val thumbnailImageView: ImageView = view.findViewById(R.id.Watch_big_thumbnailUrl)
            val dateShow: TextView = view.findViewById(R.id.Watch_video_list_data)
            val duration: TextView = view.findViewById(R.id.Watch_video_list_duration)
            val channelName: TextView = view.findViewById(R.id.Watch_video_list_channelName)
            val viewer: TextView = view.findViewById(R.id.Watch_video_list_view)


            titleTextView.text = watchLaterLists.title
            viewer.text= watchLaterLists.viewer
            dateShow.text= watchLaterLists.dateTime
            duration.text= watchLaterLists.duration
            channelName.text= watchLaterLists.channelName


            Glide.with(context)
                .load(watchLaterLists.thumbnailUrl)
                .placeholder(R.mipmap.ic_launcher_ofme)
                .centerCrop()
                .into(thumbnailImageView)
            return view

        }

        fun removeItem(position: Int, videoId: String) {
            val dbHelper = DatabaseFavorite(context)
            dbHelper.deleteWatchUrl(videoId)
            watchLaterLists.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
    }

}