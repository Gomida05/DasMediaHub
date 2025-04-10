package com.das.forui.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import com.das.forui.databased.PathSaver
import com.das.forui.objectsAndData.SettingsDataClass



@Composable
fun SettingsComposable(
  navController: NavController
) {
  val context = LocalContext.current
  val settingsResults = remember { mutableStateOf<List<SettingsDataClass>>(emptyList()) }

  val item = listOf(
    "Account",
    "Appearance",
    "Change Downloading Location",
    "Check for update",
    "About Us"
  )
  val leftIcons = listOf(
    Icons.Default.AccountCircle,
    Icons.Default.ColorLens,
    Icons.Default.Folder,
    Icons.Default.Update,
    Icons.Default.Info
  )

  val rightIcons = listOf(
    Icons.AutoMirrored.Default.ArrowForward,
    Icons.AutoMirrored.Default.ArrowForward,
    Icons.AutoMirrored.Default.ArrowForward,
    Icons.AutoMirrored.Default.ArrowForward,
    Icons.AutoMirrored.Default.ArrowForward
  )
  val allItems = item.zip(leftIcons).zip(rightIcons) { titleIconPair, rightIcon ->
    SettingsDataClass(
      title = titleIconPair.first,
      leftIcon = titleIconPair.second,
      rightIcon = rightIcon
    )
  }
  settingsResults.value = allItems
  var selectedFolder by remember { mutableIntStateOf(0) }

  if (selectedFolder != 0){
    SelectFolder(selectedFolder, context)
  }
  Scaffold { paddingValue ->


    Box(
      modifier = Modifier
        .wrapContentSize(Alignment.TopCenter)
        .padding(paddingValue)
    ) {
      Text(
        text = "Setting",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 50.dp)
          .align(Alignment.Center)
      )
    }

    LazyColumn(
      modifier = Modifier
        .wrapContentSize(Alignment.Center)
        .padding(top = 150.dp)
        .fillMaxWidth()
    ) {

      items(settingsResults.value) { settingsItem ->
        CategoryItems(
          mContext = context,
          navController = navController,
          title = settingsItem.title,
          leftHandIcon = settingsItem.leftIcon,
          rightHandIcon = settingsItem.rightIcon,
          openFileExplore = {
            selectedFolder = it
          }
        )

      }


    }
  }

}


@Composable
fun SelectFolder(type: Int, context: Context){

  val selectFor = if (type == 1) "video" else "audio"

  rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = { uri: Uri? ->
      uri?.let {

        getFolderPathFromUri(context, it, selectFor)
      }
    }
  )
}



@Composable
fun CategoryItems(
    mContext: Context,
    navController: NavController,
    title: String,
    leftHandIcon: ImageVector,
    rightHandIcon: ImageVector,
    openFileExplore: (isForVideos: Int) -> Unit
  ) {

  Card(
    onClick = {
      gotClicks(
        mContext,
        navController,
        title,
        openFileExplore = {
          openFileExplore(it)
        }
      )
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 2.dp, bottom = 2.dp)
      .clip(RoundedCornerShape(30))

  ) {


    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterHorizontally)
        .padding(35.dp)
    ) {
      Icon(
        imageVector = leftHandIcon,
        "",
        modifier = Modifier
          .align(Alignment.CenterStart)
      )
      Text(
        text = title,
        fontSize = 16.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        rightHandIcon,
        "",
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }

  }

}


private fun gotClicks(
  mContext:Context,
  navController: NavController,
  title: String,
  openFileExplore: (isForVideos: Int) -> Unit
  ) {

  when (title) {
    "Account" -> {
      showDialogs(mContext,"coming soon")
    }

    "Change Downloading Location" -> {
      alertDialogPathChoose(
        mContext,
        openFileExplore= {
          openFileExplore(it)
        }
      )
    }

    "Check for update" -> {
      showDialogs(mContext,"coming soon")
    }

    "Appearance" -> {
      navController.navigate("user Setting")
    }

    "About Us" -> {
      goToWeb(mContext)
    }
  }
}



private fun goToWeb(mContext: Context) {
  val browserIntent = Intent(
    Intent.ACTION_VIEW,
    Uri.parse("https://myfirstpythontokotlinasbackend.onrender.com/aboutdownloader")
  )
  mContext.startActivity(browserIntent)
}

private fun showDialogs(context: Context, inputText: String) {
  Toast.makeText(context, inputText, Toast.LENGTH_SHORT).show()
}



private fun alertDialogPathChoose(
  mContext: Context,
  openFileExplore: (isForVideos: Int) ->Unit
  ){

    AlertDialog.Builder(mContext)
      .setMessage("Which location do you want to change it please select on of them")
      .setNegativeButton("Video's") { _, _ ->
        openFileExplore(1)
//        openFolderPicker(1)
      }
      .setPositiveButton("Audio's") { _, _ ->
        openFileExplore(0)
//        openFolderPicker(0)
      }
      .show()


  }




  private fun extractFolderPath(path: String): String {
    val prefix = "/tree/primary:"
    return if (path.startsWith(prefix)) {
      path.removePrefix(prefix)
    } else {
      path
    }
  }






  private fun getFolderPathFromUri(mContext: Context, uri: Uri, type: String): String? {
    val path = uri.path

    try {

      val documentFile = DocumentFile.fromTreeUri(mContext, uri)


      if (documentFile != null && documentFile.isDirectory) {

        val pather="/storage/emulated/0/${extractFolderPath(path.toString())}"
        if (type == "video") {

          PathSaver().setMoviesDownloadPath(mContext, pather)

        } else if (type == "audio") {

          PathSaver().setAudioDownloadPath(mContext, pather)

        }
      } else {
        println("URI is not a directory or invalid")
      }
    } catch (e: Exception) {
      e.printStackTrace()
      println("Error: ${e.message}")
    }

    return path
  }






