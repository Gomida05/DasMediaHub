package com.das.forui.ui.downloads

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.das.forui.objectsAndData.DownloadedListData
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DownloadsPageViewModel(application: Application) : AndroidViewModel(application) {

    val downloadedListData = MutableLiveData<List<DownloadedListData>>()

    fun fetchDataFromDatabase(pathLocation: String, fileType: Int) {
        viewModelScope.launch {
            val downloadedListData = mutableListOf<DownloadedListData>()
            val pathOfVideos = File(pathLocation)
            if (pathOfVideos.exists()) {
                pathOfVideos.listFiles()?.forEach { file ->
                    val lastModified = file.lastModified()
                    val formattedDate = formatDate(lastModified)
                    val fileSizeFormatted = formatFileSize(file.length())
                    downloadedListData.add(
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
                this@DownloadsPageViewModel.downloadedListData.postValue(downloadedListData)
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