package com.das.forui.ui.downloads

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das.forui.objectsAndData.ForUIDataClass.DownloadedListData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadsPageViewModel : ViewModel() {

    private val _downloadedListData = MutableStateFlow<List<DownloadedListData>>(emptyList())
    val downloadedListData: StateFlow<List<DownloadedListData>> = _downloadedListData

    fun fetchDataFromDatabase(pathLocation: String, fileType: Int) {
        viewModelScope.launch {
            val downloadedList = mutableListOf<DownloadedListData>()
            val pathOfFiles = File(pathLocation)
            if (pathOfFiles.exists()) {
                pathOfFiles.listFiles()?.forEach { file ->
                    val lastModified = file.lastModified()
                    val formattedDate = formatDate(lastModified)
                    val fileSizeFormatted = formatFileSize(file.length())
                    downloadedList.add(
                        DownloadedListData(
                            title = file.name,
                            pathOfVideo = file.toUri(),
                            thumbnailUri = file.toUri(),
                            dateTime = formattedDate,
                            fileSize = fileSizeFormatted,
                            type = fileType
                        )
                    )
                }
                _downloadedListData.value = downloadedList
            }
        }
    }


    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }


    private fun formatFileSize(sizeInBytes: Long): String {
        return when {
            sizeInBytes >= 1_073_741_824 -> String.format(Locale.ROOT, "%.2f GB", sizeInBytes / 1_073_741_824.0)
            sizeInBytes >= 1_048_576 -> String.format(Locale.ROOT, "%.2f MB", sizeInBytes / 1_048_576.0)
            sizeInBytes >= 1_024 -> String.format(Locale.ROOT, "%.2f KB", sizeInBytes / 1_024.0)
            else -> String.format(Locale.ROOT, "%d bytes", sizeInBytes)
        }
    }
}