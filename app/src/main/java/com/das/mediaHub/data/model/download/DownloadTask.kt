package com.das.mediaHub.data.model.download

data class DownloadTask(
    val id: String,
    val url: String,
    val title: String,
    val type: DownloadType,
    val destinationPath: String,
    val headers: Map<String, String> = emptyMap(),
    val playlistName: String? = null
) {
    companion object {
        fun fromDownloadState(state: DownloadState): DownloadTask {
            return DownloadTask(
                id = state.id,
                url = state.url,
                title = state.title,
                type = state.type,
                destinationPath = state.destinationPath,
                playlistName = state.playlistName
            )
        }
    }
}
