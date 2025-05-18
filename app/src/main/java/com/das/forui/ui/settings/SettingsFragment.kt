package com.das.forui.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import com.das.forui.databased.PathSaver.setMoviesDownloadPath
import com.das.forui.databased.PathSaver.setAudioDownloadPath


@Composable
fun SettingsComposable(
  navController: NavController
) {
  val context = LocalContext.current

  Scaffold(
    topBar = {
      Text(
        text = "Setting",
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 50.dp)
      )
    }
  ) { paddingValue ->



    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(paddingValue)
        .padding(top = 65.dp)
        .wrapContentSize(Alignment.Center)
    ) {
      Saved(navController)
      Account(context)
      Appearance(navController)
      Change_Downloading_Location(context)
      Check_for_update(context)
      About_Us(context)


    }
  }

}






@Composable
private fun Saved(navController: NavController){
  Card(
    onClick = {
      navController.navigate("saved")
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(RoundedCornerShape(25))

  ) {


    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterHorizontally)
        .padding(25.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Save,
        "",
        modifier = Modifier
          .align(Alignment.CenterStart)
      )
      Text(
        text = "Saved Videos",
        fontSize = 16.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        "",
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }

  }
}

@Composable
private fun Account(context: Context){
  Card(
    onClick = {
      openMusicApp(context)
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(RoundedCornerShape(25))

  ) {


    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterHorizontally)
        .padding(25.dp)
    ) {
      Icon(
        imageVector = Icons.Default.AccountCircle,
        "",
        modifier = Modifier
          .align(Alignment.CenterStart)
      )
      Text(
        text = "Account",
        fontSize = 16.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        "",
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }

  }
}


@Composable
private fun Change_Downloading_Location(mContext: Context){


  var showAlertDialog by remember { mutableStateOf(false) }
  Card(
    onClick = {
      showAlertDialog = true

    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(RoundedCornerShape(30))

  ) {


    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterHorizontally)
        .padding(25.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Folder,
        "",
        modifier = Modifier
          .align(Alignment.CenterStart)
      )
      Text(
        text = "Change Downloading Location",
        fontSize = 16.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        "",
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }

  }
  FolderPickerDialog(mContext, showAlertDialog, onDismiss = { showAlertDialog = false })

}


@Composable
private fun Check_for_update(mContext: Context){
  Card(
    onClick = {
      showDialogs(mContext)
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(RoundedCornerShape(30))

  ) {


    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterHorizontally)
        .padding(25.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Update,
        "",
        modifier = Modifier
          .align(Alignment.CenterStart)
      )
      Text(
        text = "Check for update",
        fontSize = 16.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        "",
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }

  }
}

@Composable
private fun Appearance(navController: NavController){
  Card(
    onClick = {
      navController.navigate("user Setting")
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(RoundedCornerShape(30))

  ) {


    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterHorizontally)
        .padding(25.dp)
    ) {
      Icon(
        imageVector = Icons.Default.ColorLens,
        "",
        modifier = Modifier
          .align(Alignment.CenterStart)
      )
      Text(
        text = "Appearance",
        fontSize = 16.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        "",
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }

  }
}

@Composable
private fun About_Us(mContext: Context){
  Card(
    onClick = {
      goToWeb(mContext)
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(RoundedCornerShape(30))

  ) {


    Box(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.CenterHorizontally)
        .padding(25.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Info,
        "",
        modifier = Modifier
          .align(Alignment.CenterStart)
      )
      Text(
        text = "About US",
        fontSize = 16.sp,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        "",
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }

  }
}





private fun goToWeb(mContext: Context) {

  val url = Uri.parse("https://gomida05.github.io/")

  val browserIntent = Intent(Intent.ACTION_VIEW, url)

  mContext.startActivity(browserIntent)
}

private fun showDialogs(context: Context, inputText: String = "coming soon") {
  Toast.makeText(context, inputText, Toast.LENGTH_SHORT).show()
}

fun openMusicApp(context: Context){
  try {
    val musicApp = context.packageManager.getLaunchIntentForPackage("com.das.musicplayer")
    context.startActivity(musicApp)
  }catch (p: PackageManager.NameNotFoundException){
    showDialogs(context, "App Not founded!")
  }
  catch (e: Exception){
    showDialogs(context,"App not opening!")
  }
}


@Composable
fun AlertDialogPathChoose(
  onDismissRequest: () -> Unit = {},
  onAudioSelect: () -> Unit,
  onVideoSelect: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
      Text("Which location do you want to change? Please select one of them:")
    },
    confirmButton = {
      TextButton(onClick = {
        onAudioSelect()
        onDismissRequest()
      }) {
        Text("Audio's")
      }
    },
    dismissButton = {
      TextButton(onClick = {
        onVideoSelect()
        onDismissRequest()
      }) {
        Text("Video's")
      }
    }
  )
}


@Composable
fun FolderPickerDialog(context: Context, showDialog: Boolean, onDismiss: () -> Unit) {

  val audioPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = { uri: Uri? ->
      uri?.let {
        getFolderPathFromUri(context, it, "audio")
      }
    }
  )

  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = { uri: Uri? ->
      uri?.let {
        getFolderPathFromUri(context, it, "video")
      }
    }
  )

  if (showDialog) {

    AlertDialogPathChoose(
      onDismissRequest = onDismiss,
      onAudioSelect = {
        audioPickerLauncher.launch(null)
      },
      onVideoSelect = {
        videoPickerLauncher.launch(null)
      }
    )
  }
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

          setMoviesDownloadPath(mContext, pather)

        } else if (type == "audio") {

          setAudioDownloadPath(mContext, pather)

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