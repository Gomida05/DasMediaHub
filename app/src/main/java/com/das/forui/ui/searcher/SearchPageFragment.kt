package com.das.forui.ui.searcher

import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.CustomTheme
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databinding.FragmentSearcherBinding
import com.das.forui.MainActivity.Youtuber.extractor
import com.das.forui.databased.SearchHistoryDB


class SearchPageFragment : Fragment() {

    private var _binding: FragmentSearcherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearcherBinding.inflate(inflater, container, false)

        binding.searchHistoryFromComposeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CustomTheme {
                    ListSearchHistoryItem()
                }
            }
        }

        val suggestions = listOf("Apple", "Banana", "Cherry", "Date", "Grape", "Kiwi", "Lemon", "Mango", "Peach", "Plum")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)

        binding.mySearchTextInput.setAdapter(adapter)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showKeyboard(binding.mySearchTextInput)
    }
    override fun onStart() {
        super.onStart()

        binding.exitSearch.setOnClickListener{
            findNavController().navigateUp()
        }



        val textInput = binding.mySearchTextInput
        textInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && textInput.text.isNotBlank()) {
                // Call the function that handles the search action
                keyEvent(textInput.text.toString())
                hideKeyboard(binding.mySearchTextInput)
                true
            } else {
                false
            }
        }

        textInput.addTextChangedListener(TextWatcherListener())

        if (textInput.text.isNotEmpty()){
            binding.clearText.visibility = View.GONE
        }

        binding.clearText.setOnClickListener { i->
            textInput.text.clear()
            i.visibility = View.GONE
        }
    }






    @Composable
    private fun ListSearchHistoryItem(){


        val settingsResults = remember { mutableStateOf<List<String>>(emptyList()) }

        LaunchedEffect(Unit) {
            settingsResults.value = fetchDataFromDatabase()
        }
        LazyColumn(
            modifier = Modifier
        ) {
            items(settingsResults.value) { settingsItem ->
                CategoryItems(
                    title = settingsItem,
                    onDelete = { title->
                        // Show the confirmation dialog
                        SearchHistoryDB(requireContext()).deleteSearchList(title)
                        settingsResults.value = settingsResults.value.filter { it != settingsItem }
                        // Call the database function to delete
                    }
                )
            }
        }
    }

    @Composable
    private fun CategoryItems(title : String, onDelete: (title: String) -> Unit){

        Button(
            onClick = {
                binding.mySearchTextInput.setText(title)
                goSearch(title)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(top = 2.dp, bottom = 2.dp)
        ) {
            Box(
            modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = {

                        AlertDialog.Builder(requireContext())
                            .setTitle("Are you sure you want to remove it from the list?")
                            .setPositiveButton("Yes") { _, _ ->
                                onDelete(title)
                            }
                            .setNegativeButton("No") { _, _ ->
                            }
                            .show()


                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.Delete),
                        ""
                    )
                }
            }


        }
    }






    private fun keyEvent(editTextText: String) {
        val bundle = Bundle().apply { putString("EXTRA_TEXT", editTextText) }

        try {
            if ((activity as MainActivity).isValidYoutubeURL(editTextText)) {
                val videoId = extractor(editTextText)
                val bundled = bundle.apply {
                    putString("View_ID", videoId)
                    putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                }
                findNavController().navigate(R.id.nav_video_viewer, bundled)
            } else {
                SearchHistoryDB(requireContext()).insertData(title = editTextText)
                goSearch(editTextText)
            }
        } catch (e: Exception) {
            (activity as MainActivity).showDialogs(e.message.toString())
        }
    }

    private fun goSearch(text: String) {
        try {
            val bundle = Bundle().apply { putString("EXTRA_TEXT", text) }
            findNavController().navigate(R.id.nav_result, bundle)
        } catch (e: Exception) {
            (activity as MainActivity).showDialogs(e.message.toString())
        }
    }


    private fun fetchDataFromDatabase(): List<String> {
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

            return urls.toList()
        }catch (e:Exception){
            val alerting= (activity as MainActivity)
            alerting.showDialogs(e.message.toString())
            alerting.alertUserError(e.message.toString())
        }
        return listOf("")

    }

    private fun showKeyboard(editText: EditText) {
        editText.requestFocus()

        // Get the InputMethodManager service
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Show the soft keyboard
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }


    private fun hideKeyboard(editText: EditText) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
    override fun onResume() {
        super.onResume()
        if (binding.mySearchTextInput.text.isNotEmpty()){
            binding.clearText.visibility = View.VISIBLE
        }
        (activity as MainActivity).hideBottomNav()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private inner class TextWatcherListener: TextWatcher {
        override fun beforeTextChanged(
            charSequence: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {

        }

        override fun onTextChanged(
            charSequence: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {


        }

        override fun afterTextChanged(editable: Editable?) {

            if (editable?.isEmpty() == true) {
                binding.clearText.visibility = View.GONE
            } else {
                binding.clearText.visibility = View.VISIBLE
            }
        }
    }
}
