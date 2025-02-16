package com.das.forui.ui.downloads

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.das.forui.databased.DatabaseHelper1
import com.das.forui.FullScreenPlayerActivity
import com.das.forui.MainActivity
import com.das.forui.services.MyService
import com.das.forui.R
import com.das.forui.databinding.FragmentDownloadsBinding

class DownloadsPageFragment: Fragment() {
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ArrayAdapter<String>
    private val ids = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        fetchDataFromDatabase()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.back.setOnClickListener {
            binding.root.findNavController().navigate(R.id.navigation_home)
        }
        binding.button2.setOnClickListener {
            (activity as MainActivity).startBanner(false)
            val intent = Intent(requireContext(), MyService::class.java)
            requireContext().stopService(intent)

        }



        val listView: ListView = binding.downloadedHistory
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedId= ids[position]
            if(selectedId.endsWith(".mp3")) {
                val shareIntent = Intent(Intent.ACTION_VIEW) .apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_TEXT, selectedId)
                }
                shareIntent.setClass(requireContext(), FullScreenPlayerActivity::class.java)
                startActivity(shareIntent)
            } else if(selectedId.endsWith(".mp4")) {
                val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_TEXT, selectedId)
                }
                shareIntent.setClass(requireContext(), FullScreenPlayerActivity::class.java)
                startActivity(shareIntent)
            } else{
                (activity as MainActivity).showDiaglo("We don't support this type of file")
            }

        }
    }

        override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showBottomNav()
    }


    private fun fetchDataFromDatabase() {
        val dbHelper = DatabaseHelper1(requireContext())

        val cursor: Cursor? = dbHelper.getResults()

        Log.d("DownloadsFragment", "Query executed, cursor count: ${cursor?.count ?: "null"}")
        val urls = mutableListOf<String>()

        ids.clear()
        cursor?.let {
            while (it.moveToNext()) {
                val path = it.getString(it.getColumnIndexOrThrow("path"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                path?.let { url ->
                    urls.add("$title ")
                    ids.add(url)
                    Log.d("DownloadsFragment", "Watch URL: $title")
                } ?: run {
                    Log.e("DownloadsFragment", "Watch URL is null")
                }
            }
            it.close()
        } ?: run {
            Log.e("DownloadsFragment", "Cursor is null")
        }
            Log.d("DownloadsFragment", "URLs collected: $urls")

            if (urls.isNotEmpty()) {
                adapter.clear()
                adapter.addAll(urls)
                adapter.notifyDataSetChanged()
            } else {
                Log.e("DownloadsFragment", "URLs list is empty or null")
            }
    }










    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
