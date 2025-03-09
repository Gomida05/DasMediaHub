package com.das.forui.ui.searcher

import android.app.AlertDialog
import android.content.Intent.EXTRA_TEXT
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databinding.FragmentSearcherBinding
import androidx.lifecycle.viewmodel.compose.viewModel
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
                ListSearchHistoryItem()
            }
        }
        binding.editTextCompose.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ShowTextField()
            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    @Composable
    private fun ShowTextField() {
        val searchViewModel: SearchViewModel = viewModel()

        TextField(
            value = searchViewModel.searchText,
            onValueChange = { searchViewModel.updateSearchText(it) },
            shape = RoundedCornerShape(17.dp),
            singleLine = true,
            placeholder = { Text(text = "Enter search text") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyEvent(searchViewModel.searchText)
                }
            ),
            trailingIcon = {
                if (searchViewModel.searchText.isNotEmpty()) {
                    IconButton(onClick = {
                        searchViewModel.updateSearchText("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Text")
                    }
                }
            },
            leadingIcon = {
                Button(onClick = {
                    findNavController().navigateUp()
                },
                    colors = ButtonColors(Color.Black, Color.White, Color.Unspecified, Color.Unspecified),
                    modifier = Modifier
                ) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "navigator")
                }
            },
            modifier = Modifier
                .background(Color(0xFF03DAC5), RoundedCornerShape(15.dp))
        )
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
                    onDelete = {
                        // Show the confirmation dialog
                        SearchHistoryDB(requireContext()).deleteSearchList(settingsItem)
                        settingsResults.value = settingsResults.value.filter { it != settingsItem }
                        // Call the database function to delete
                    }
                )
            }
        }
    }

    @Composable
    private fun CategoryItems(title : String, onDelete: () -> Unit){

        Button(
            onClick = {
                SearchViewModel().updateSearchText(title)
                goSearch(title)
            },
            colors = ButtonColors(Color.Black, Color.White, Color.Red, Color.Blue),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(top = 2.dp, bottom = 2.dp)
                .background(Color.Black, RoundedCornerShape(20.dp))
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
                                onDelete()
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



    override fun onResume() {
        super.onResume()
        (activity as MainActivity).hideBottomNav()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


class SearchViewModel : ViewModel() {

    var searchText by mutableStateOf("")

    fun updateSearchText(newText: String) {
        searchText = newText

    }
}