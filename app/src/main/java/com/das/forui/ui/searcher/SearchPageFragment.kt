package com.das.forui.ui.searcher

import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.das.forui.MainApplication.Youtuber.youtubeExtractor
import com.das.forui.MainApplication.Youtuber.isValidYoutubeURL
import com.das.forui.databased.SearchHistoryDB
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_INTENT_FOR_VIEWER
import com.das.forui.objectsAndData.ForUIKeyWords.NEW_TEXT_FOR_RESULT
import com.das.forui.ui.viewer.GlobalVideoList.bundles


@Composable
fun SearchPageCompose(
    navController: NavController,
    newText: String
) {

    val textState = remember { mutableStateOf(newText) }


    val context = LocalContext.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = textState.value,
                onValueChange = { newText ->
                    textState.value = newText
                },
                placeholder = {
                    Text(
                        text = "Enter key words or Insert URL"
                    )
                },
                shape = RoundedCornerShape(28),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .align(Alignment.CenterHorizontally),

                textStyle = MaterialTheme.typography.bodyMedium,
                leadingIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.AutoMirrored.Default.ArrowBack),
                            "navigateUpButton"
                        )
                    }
                },
                trailingIcon = {
                    if (textState.value.isNotEmpty()) {
                        IconButton(onClick = { textState.value = "" }) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Default.Close),
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    autoCorrectEnabled = true,
                    keyboardType = KeyboardType.Text,
                    showKeyboardOnFocus = true
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (textState.value.isNotBlank()) {
                            keyEvent(
                                navController,
                                textState.value,
                                context
                            )
                        }
                    }
                )
            )



            Spacer(modifier = Modifier.height(16.dp))

            val settingsResults = remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(Unit) {
                settingsResults.value = fetchDataFromDatabase(context)
            }
            LazyColumn(
                modifier = Modifier
            ) {
                items(settingsResults.value) { settingsItem ->
                    CategoryItems(
                        context,
                        title = settingsItem,
                        settingsResults,
                        onButtonClicked = { text ->
                            textState.value = text
                            goSearch(context, navController, text)
                        }
                    )
                }
            }


        }

    }

}




@Composable
private fun CategoryItems(
    context: Context,
    title: String,
    settingsResults: MutableState<List<String>>,
    onButtonClicked: (text: String)-> Unit
) {

    Button(
        onClick = {
            onButtonClicked(title)
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

                    AlertDialog.Builder(context)
                        .setTitle("Are you sure you want to remove it from the list?")
                        .setPositiveButton("Yes") { _, _ ->
                            SearchHistoryDB(context).deleteSearchList(title)
                            settingsResults.value = settingsResults.value.filter { it != title }
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






private fun keyEvent(
    navController: NavController,
    editTextText: String,
    context: Context
) {


        try {
            if (isValidYoutubeURL(editTextText)) {
                val videoId = youtubeExtractor(editTextText)
                val bundled = Bundle().apply {
                    putString("View_ID", videoId)
                    putString("View_URL", "https://www.youtube.com/watch?v=$videoId")
                }
                bundles.putBundle(NEW_INTENT_FOR_VIEWER, bundled)
                navController.navigate("video viewer")
            } else {
                SearchHistoryDB(context).insertData(title = editTextText)
                goSearch(
                    context,
                    navController,
                    editTextText
                )
            }
        } catch (e: Exception) {
            showDialogs(context,e.message.toString())
        }
    }

    private fun goSearch(
        context: Context,
        navController: NavController,
        text: String
    ) {
        try {
            bundles.putString(NEW_TEXT_FOR_RESULT, text)
            navController.navigate("ResultViewerPage")
        } catch (e: Exception) {
            showDialogs(context, e.message.toString())
        }
    }

private fun showDialogs(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun fetchDataFromDatabase(context: Context): List<String> {
    try {
        val dbHelper = SearchHistoryDB(context)

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
    } catch (e: Exception) {
        showDialogs(context, e.message.toString())
    }
    return listOf("")

}




