@file:Suppress("DEPRECATION")
package com.das.forui.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.CustomTheme
import com.das.forui.MainActivity
import com.das.forui.databased.PathSaver
import com.das.forui.R
import com.das.forui.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

  private var _binding: FragmentSettingsBinding? = null
  private val folderPickerRequestCode= 0
  private val folderPickerForMyVideo= 1
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {


    _binding = FragmentSettingsBinding.inflate(inflater, container, false)


    return binding.root
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.settingsListFromComposeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        CustomTheme {
          ListSettingsItem()
        }
      }
    }
  }

  @Composable
  private fun ListSettingsItem() {
    val settingsResults = remember { mutableStateOf<List<SettingsDataClass>>(emptyList()) }

    val item = listOf(
      "Setting for searchList",
      "HeadLine",
      "Change Downloading Location",
      "Check for update",
      "About Us"
    )
    val leftIcons = listOf(
      Icons.Default.ManageSearch,
      Icons.Default.VideoLibrary,
      Icons.Default.Folder,
      Icons.Default.Update,
      Icons.Default.Info
    )
    val rightIcons = listOf(
      Icons.Default.ArrowForward,
      Icons.Default.ArrowForward,
      Icons.Default.ArrowForward,
      Icons.Default.ArrowForward,
      Icons.Default.ArrowForward
    )
    val allItems = item.zip(leftIcons).zip(rightIcons) { titleIconPair, rightIcon ->
      SettingsDataClass(
        title = titleIconPair.first,
        leftIcon = titleIconPair.second,
        rightIcon = rightIcon
      )
    }
    settingsResults.value = allItems

    LazyColumn(
      modifier = Modifier
    ) {

      items(settingsResults.value) { settingsItem ->
        CategoryItems(
          title = settingsItem.title,
          leftHandIcon = settingsItem.leftIcon,
          rightHandIcon = settingsItem.rightIcon

        )
      }
    }
  }

    @Composable
    fun CategoryItems(
      title: String,
      leftHandIcon: ImageVector,
      rightHandIcon: ImageVector
    ){
      var showAlertDialog by remember { mutableStateOf(false) }

      Button(
        onClick = {showAlertDialog = true},
        modifier = Modifier
          .clip(RoundedCornerShape(1))
          .fillMaxWidth()
          .height(80.dp)
          .padding(top = 2.dp, bottom = 2.dp)
      ) {
        if (showAlertDialog){
          GotClicks(title)
        }
        Box(
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = leftHandIcon,
            "",
            modifier = Modifier.align(Alignment.CenterStart)
          )
          Text(
            text = title,
            fontSize = 16.sp,
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


  @Composable
  private fun GotClicks(title: String){


    when (title) {
      "Setting for searchList" -> {
        findNavController().navigate(R.id.nav_result)
      }

      "HeadLine" -> {

      }

      "Change Downloading Location" -> {
        AlertDialogPathChoose(true)
      }

      "About Us" -> {
        goToWeb()
      }

      "Check for update" -> {
        (activity as MainActivity).showDialogs("coming soon")
      }
    }
  }



  private fun goToWeb() {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myfirstpythontokotlinasbackend.onrender.com/aboutdownloader"))
    startActivity(browserIntent)
  }



  @Composable
  private fun AlertDialogPathChoose(soTruOrFalse: Boolean) {

    var openDialog by remember { mutableStateOf(soTruOrFalse) }


    if (openDialog) {
      AlertDialog(
        onDismissRequest = { openDialog = false },
        title = { Text("Which location do you want to change it please select on of them") },
        confirmButton = {
          TextButton(
            onClick = {
              openFolderPicker(folderPickerForMyVideo)
              // Handle the confirm button action
            }
          ) {
            Text("Video's")
          }
          TextButton(
            onClick = {
              openFolderPicker(folderPickerRequestCode)
            }
          ) {
            Text("Audio's")
          }
        },
        dismissButton = {
          TextButton(
            onClick = {
              openDialog = false
            }
          ) {
            Text("Cancel")
          }
        }
      )
    }
  }
  
  private fun openFolderPicker(code: Int) {
    // Create an intent to pick a folder
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    startActivityForResult(intent, code)
  }

  private fun extractFolderPath(path: String): String {
    val prefix = "/tree/primary:"
    return if (path.startsWith(prefix)) {
      path.removePrefix(prefix)
    } else {
      path
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    val uri: Uri = data?.data!!
    if (requestCode == folderPickerRequestCode && resultCode == Activity.RESULT_OK) {

      getFolderPathFromUri(requireContext(), uri, "audio")
    }
    else if (requestCode == folderPickerForMyVideo && resultCode == Activity.RESULT_OK) {

      getFolderPathFromUri(requireContext(), uri, "video")

    }
  }




  private fun getFolderPathFromUri(context: Context, uri: Uri, type: String): String? {
    val path = uri.path

    try {

      val documentFile = DocumentFile.fromTreeUri(context, uri)


      if (documentFile != null && documentFile.isDirectory) {

        val pather="/storage/emulated/0/${extractFolderPath(path.toString())}"
        if (type == "video") {

          PathSaver().setMoviesDownloadPath(requireContext(), pather)

        } else if (type == "audio") {

          PathSaver().setAudioDownloadPath(requireContext(), pather)

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



  data class SettingsDataClass (
    val title: String,
    val leftIcon: ImageVector,
    val rightIcon: ImageVector

  )



  override fun onResume() {
    super.onResume()
    (activity as MainActivity).showBottomNav()
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}


