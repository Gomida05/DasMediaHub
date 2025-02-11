package com.das.forui.ui.searcher

import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.SearchHistoryDB
import com.das.forui.databinding.FragmentSearcherBinding



class SearchPageFragment : Fragment() {

    private var _binding: FragmentSearcherBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ArrayAdapter<String>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearcherBinding.inflate(inflater, container, false)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, mutableListOf())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchDataFromDatabase()
        val sentText=arguments?.getString("EXTRA_ONE")
//        binding.editTextText.text= sentText.toString() as Editable?
        if (sentText=="Search12.23.58/'[0][-"){

        }else {
            binding.editTextText.setText(sentText)
        }
        binding.searchHistory.adapter = adapter

    }


    override fun onStart() {
        super.onStart()



        val textEdit: EditText = binding.editTextText
        binding.searchHistory.setOnItemLongClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            removeIt(selectedItem)
            true
        }


        binding.searchHistory.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            textEdit.setText(selectedItem)
            goSearch(selectedItem)
        }




        textEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    addTheButton()
                } else {
                    removeTheButton()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // No action needed here

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })
        val editText: EditText = binding.editTextText
        editText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                if (editText.text.isNotBlank()) {
                    keyEvent()
                    true
                } else {
                    textEdit.post {
                        editText.requestFocus()
                        editText.setSelection(textEdit.text.length)

                    }

                    Toast.makeText(
                        context,
                        "Please enter some text before proceeding.",
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                }
            } else {
                false
            }
        }





        val clear= binding.clear


        binding.back.setOnClickListener {
            it.isEnabled = false
            findNavController().navigateUp()
        }

        clear.setOnClickListener {
            textEdit.text.clear()
            removeTheButton()
//            SearchHistoryDB(requireContext()).deleteWatchUrl()
        }


    }





    private fun removeIt(selectedItem: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to remove it from the list?")
            .setPositiveButton("Yes") { _, _ ->
                removeList(selectedItem)
            }
            .setNegativeButton("No") { _, _ ->
            }
            .show()

    }



    private fun removeList(selectedItem: String) {
        adapter.remove(selectedItem)
        SearchHistoryDB(requireContext()).deleteSearchList(selectedItem)
        adapter.notifyDataSetChanged()
    }

    private fun keyEvent() {
        val editTextText = binding.editTextText.text.toString()
        val bundle = Bundle().apply { putString("EXTRA_TEXT", editTextText) }

        try {
            if ((activity as MainActivity).isValidYoutubeURL(editTextText)) {
                val videoId = MainActivity.Youtuber.extractor(editTextText)
                val bundled=bundle.apply {
                    putString("View_ID", videoId)
                    putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                }
                findNavController().navigate(R.id.nav_video_viewer, bundled)
            } else {
                SearchHistoryDB(requireContext()).insertData(title = editTextText)
                goSearch(editTextText)
            }
        } catch (e: Exception) {
            (activity as MainActivity).showDiaglo(e.message.toString())
        }
    }

    private fun goSearch(text: String) {
        try {
            val bundle = Bundle().apply { putString("EXTRA_TEXT", text) }
            findNavController().navigate(R.id.nav_result, bundle)
        } catch (e: Exception) {
            (activity as MainActivity).showDiaglo(e.message.toString())
        }
    }



    private fun removeTheButton() {
//        binding.editTextText.drawable
        binding.clear.visibility= View.GONE
    }

    fun addTheButton() {
        binding.clear.visibility= View.VISIBLE
    }

    private fun fetchDataFromDatabase() {
        try {
            val dbHelper = SearchHistoryDB(requireContext())

            val cursor: Cursor? = dbHelper.getResults()
            val urls = mutableListOf<String>()
            cursor?.let {
                while (it.moveToNext()) {
                    val title = it.getString(it.getColumnIndexOrThrow("title"))
                    title?.let { _ ->
                        urls.add("$title ")
                    } ?: run {}
                }
                it.close()
            } ?: run {}

            if (urls.isNotEmpty()) {
                adapter.clear()
                adapter.addAll(urls)
                adapter.notifyDataSetChanged()
            } else {
            }
        }catch (e:Exception){
            val alerting= (activity as MainActivity)
            alerting.showDiaglo(e.message.toString())
            alerting.alertUserError(e.message.toString())
        }

    }



    override fun onResume() {
        super.onResume()
        val textEdit: EditText = binding.editTextText
        if(textEdit.toString().isNotEmpty()){
            addTheButton()
        }
        textEdit.requestFocus()
        textEdit.post {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            @Suppress("DEPRECATION")
            imm.showSoftInput(textEdit, InputMethodManager.SHOW_FORCED)
        }
        (activity as MainActivity).hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        val textEdit: EditText = binding.editTextText
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(textEdit.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
