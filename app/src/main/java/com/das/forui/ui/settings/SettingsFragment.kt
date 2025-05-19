package com.das.forui.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
  ) {
    LazyColumn (
      modifier = Modifier
        .padding(it)
        .fillMaxSize()
    ) {
      item { UserHeader() }

      item {
        VerticalDivider(modifier = Modifier.padding(vertical = 3.dp))
      }

      item { Saved(navController) }
      item { Account(context) }

      item {
        VerticalDivider(modifier = Modifier.padding(vertical = 1.dp))
      }

      item { Appearance(navController) }

      item {
        VerticalDivider(modifier = Modifier.padding(vertical = 1.dp))
      }

      item { Change_Downloading_Location(context) }
      item { Check_for_update(context) }
      item { About_Us(context) }

      item {
        VerticalDivider(modifier = Modifier.padding(vertical = 1.dp))
      }

      item { FeedbackButton(context) }
      item { AppVersionInfo() }

//
//      Saved(navController)
//      Account(context)
//      Appearance(navController)


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
fun UserHeader(name: String = "Guest User", email: String = "guest@example.com") {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.AccountCircle,
      contentDescription = null,
      modifier = Modifier.size(50.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(text = name, style = MaterialTheme.typography.titleMedium)
      Text(text = email, style = MaterialTheme.typography.bodySmall)
    }
  }
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




@Composable
fun FeedbackButton(context: Context) {
  Card(
    onClick = {
      sendFeedback(context)
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(4.dp)
      .clip(RoundedCornerShape(25))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(25.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Feedback,
        contentDescription = null,
        modifier = Modifier.align(Alignment.CenterStart)
      )
      Text(
        text = "Send Feedback",
        style = MaterialTheme.typography.headlineSmall,
        fontSize = 16.sp,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = Icons.AutoMirrored.Default.ArrowForward,
        contentDescription = null,
        modifier = Modifier.align(Alignment.CenterEnd)
      )
    }
  }
}

@Composable
fun AppVersionInfo() {
  val context = LocalContext.current
  val version = remember {
    context.packageManager
      .getPackageInfo(context.packageName, 0).versionName
  }
  Text(
    text = "App Version: $version",
    style = MaterialTheme.typography.bodySmall,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    textAlign = TextAlign.Center
  )
}

fun sendFeedback(context: Context) {
  val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("mailto:efootballmobile2023player@gmail.com") // Replace with your email
    putExtra(Intent.EXTRA_SUBJECT, "Feedback for My App")
    putExtra(Intent.EXTRA_TEXT, "Hi, I have some feedback...")
  }

  try {
    context.startActivity(Intent.createChooser(intent, "Send Feedback"))
  } catch (e: ActivityNotFoundException) {
    Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
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