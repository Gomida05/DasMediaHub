@file:Suppress("DEPRECATION")
package com.das.forui.ui.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    binding.settingsListFromComposeView.apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        CustomTheme {
          ListSettingsItem()
        }
      }
    }
    return binding.root
  }




  @Composable
  private fun ListSettingsItem() {
    val settingsResults = remember { mutableStateOf<List<SettingsDataClass>>(emptyList()) }

    val item = listOf(
      "Setting for searchList",
      "Change Downloading Location",
      "Check for update",
      "About Us"
    )
    val leftIcons = listOf(
      Icons.Default.ManageSearch,
      Icons.Default.Folder,
      Icons.Default.Update,
      Icons.Default.Info
    )
    val rightIcons = listOf(
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

    LazyColumn{

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

    val interactionSource = remember { MutableInteractionSource() }


    Button(
      onClick = {
        gotClicks(title)
      },
      interactionSource = interactionSource,
      elevation = ButtonDefaults.buttonElevation(
        defaultElevation = 50.dp
      ),
      modifier = Modifier
        .focusable(interactionSource = interactionSource)
        .hoverable(interactionSource = interactionSource)
        .fillMaxWidth()
        .height(80.dp)
        .padding(top = 2.dp, bottom = 2.dp)
    ) {


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


  private fun gotClicks(title: String){

    when (title) {
      "More" -> {
        findNavController().navigate(R.id.nav_userSetting)
      }

      "Change Downloading Location" -> {
        alertDialogPathChoose()
      }

      "Check for update" -> {
        (activity as MainActivity).showDialogs("coming soon")
      }

      "About Us" -> {
        goToWeb()
      }
    }
  }



  private fun goToWeb() {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myfirstpythontokotlinasbackend.onrender.com/aboutdownloader"))
    startActivity(browserIntent)
  }




  private fun alertDialogPathChoose(){

    AlertDialog.Builder(requireContext())
      .setMessage("Which location do you want to change it please select on of them")
      .setNegativeButton("Video's") { _, _ ->
        openFolderPicker(folderPickerForMyVideo)
      }
      .setPositiveButton("Audio's") { _, _ ->
        openFolderPicker(folderPickerRequestCode)
      }
      .show()

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


