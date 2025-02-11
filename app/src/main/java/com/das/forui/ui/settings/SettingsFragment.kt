@file:Suppress("DEPRECATION")

package com.das.forui.ui.settings


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.MainActivity
import com.das.forui.PathSaver
import com.das.forui.R
import com.das.forui.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

  private var _binding: FragmentSettingsBinding? = null
  private val folderPickerRequestCode= 1
  private val folderPickerForMyVideo= 2
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {


    _binding = FragmentSettingsBinding.inflate(inflater, container, false)

    val listView: ListView = binding.settingsList
    val items = arrayOf(
      "Setting for searchList",
      "HeadLine",
      "Change Downloading Location",
      "Check for update",
      "About Us"
    )
    val adapter = CustomAdapter(requireContext(), items)
    listView.adapter = adapter
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.settingsList.setOnItemClickListener { parent, _, position, _ ->
      when (parent.getItemAtPosition(position).toString()) {
          "Setting for searchList" -> {
            findNavController().navigate(R.id.nav_result)
          }
          "HeadLine" -> {
            findNavController().navigate(R.id.nav_splash_screen)
          }
          "Change Downloading Location" -> {
            alertDialogPathChoose()
          }
          "About Us" -> {
            goToWeb()
          }
        "Check for update" -> {
          (activity as MainActivity).showDiaglo("coming soon")
          }
      }
    }

  }

  private fun goToWeb() {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myfirstpythontokotlinasbackend.onrender.com/aboutdownloader"))
    startActivity(browserIntent)
  }

  private fun alertDialogPathChoose(){
    AlertDialog.Builder(context)
      .setTitle("Which location do you want to change it please select on of them")
      .setPositiveButton("Video's"){ _, _ -> openFolderPicker(folderPickerForMyVideo)}
      .setNeutralButton("Audio's"){ _, _ -> openFolderPicker(folderPickerRequestCode)}
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

    if (requestCode == folderPickerRequestCode && resultCode == Activity.RESULT_OK) {
      val uri: Uri = data?.data!!

      // Now, get the path or perform other operations using DocumentFile
      val folderPath = getFolderPathFromUri(requireContext(), uri, "audio")
      println("Folder Path: $folderPath")
    }
    else if (requestCode == folderPickerForMyVideo && resultCode == Activity.RESULT_OK) {
      val uri: Uri = data?.data!!
      // Now, get the path or perform other operations using DocumentFile
      val folderPath = getFolderPathFromUri(requireContext(), uri, "video")
      println("Folder Path: $folderPath")

    }
  }




  private fun getFolderPathFromUri(context: Context, uri: Uri, type: String): String? {
    var path: String? = null

    try {
      // Use DocumentFile to interact with the directory URI
      val documentFile = DocumentFile.fromTreeUri(context, uri)

      // Check if the URI represents a valid directory
      if (documentFile != null && documentFile.isDirectory) {
        // The path is not directly accessible for tree URIs, but we can extract it using the URI path
        path = uri.path
        println("Selected Folder Path: $path")
        val pather="/storage/emulated/0/${extractFolderPath(path.toString())}"
        if (type=="video"){
          PathSaver().saveMoviesDownloadPath(requireContext(), pather)
        }else{
          PathSaver().saveMusicDownloadPath(requireContext(), pather)
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


  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)

  }





  override fun onResume() {
    super.onResume()
    (activity as MainActivity).showBottomNav()
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  class CustomAdapter(context: Context, private val items: Array<String>) :
    ArrayAdapter<String>(context, R.layout.list_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      val view =
        convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

      val leftIcon = view.findViewById<ImageView>(R.id.left_icon)
      val rightIcon = view.findViewById<ImageView>(R.id.right_icon)
      rightIcon.setBackgroundResource(R.drawable.textbox)
//      rightIcon.setBackgroundColor(R.color.teal_200)
      val itemText = view.findViewById<TextView>(R.id.item_text)

      // Set your icons
      leftIcon.setImageResource(R.mipmap.icon) // Replace with your actual drawable resource
      rightIcon.setImageResource(R.drawable.accpuntimg) // Replace with your actual drawable resource

      // Set your item text
      itemText.text = items[position]

      return view

    }
  }
}

