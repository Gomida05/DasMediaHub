package com.das.mediaHub.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.das.downloader.data.local.PathPreferences
import com.das.downloader.data.model.PathType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepo @Inject constructor(
    @param:ApplicationContext private val context: Context
) {



    fun persistFolderPermission(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    fun getFolderPathFromUri(uri: Uri): String? {
        return try {
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            if (documentFile != null && documentFile.isDirectory) {
                "/storage/emulated/0/${extractFolderPath(uri.path.toString())}"
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractFolderPath(path: String): String {
        return path.removePrefix("/tree/primary:")
    }

    fun updatePath(pathType: PathType, newPath: String) {
        PathPreferences.updatePath(
            context = context,
            pathType = pathType,
            newPath = newPath
        )
    }

}