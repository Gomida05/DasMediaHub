package com.das.mediaHub.data.repository

import com.das.mediaHub.data.local.dao.FavoritesDao
import com.das.mediaHub.data.model.SavedVideosListData
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(
    private val dao: FavoritesDao
) {
    suspend fun getAllSavedVideos(): List<SavedVideosListData> = dao.getAllSavedVideos()

    fun isWatchUrlExist(videoId: String): Flow<Boolean> =
        dao.isWatchUrlExist(videoId)

    suspend fun insertData(
        videoId: String,
        title: String,
        videoDate: String,
        videoViewCount: String,
        videoChannelName: String,
        duration: String,
        channelThumbnail: String
    ) {
        dao.insert(
            SavedVideosListData(
                watchUrl = videoId,
                title = title,
                views = videoViewCount,
                dateTime = videoDate,
                channelName = videoChannelName,
                duration = duration,
                channelThumbnail = channelThumbnail
            )
        )
    }

    suspend fun deleteWatchUrl(videoId: String): Int =
        dao.deleteWatchUrl(videoId)

    suspend fun clearAll() =
        dao.clearAll()
}