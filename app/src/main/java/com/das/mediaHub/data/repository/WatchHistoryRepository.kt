package com.das.mediaHub.data.repository

import com.das.mediaHub.data.local.dao.WatchHistoryDao
import com.das.mediaHub.data.model.WatchedVideoEntity
import kotlinx.coroutines.flow.Flow

class WatchHistoryRepository(
    private val dao: WatchHistoryDao
) {

    suspend fun getWatchedVideos(): List<WatchedVideoEntity> = dao.getWatchedVideos()

    suspend fun insertNewVideo(
        videoId: String,
        title: String,
        videoDate: String,
        videoViewCount: String,
        videoChannelName: String,
        duration: String,
        channelThumbnail: String
    ): Boolean {
        if (dao.exists(videoId)) return false

        if (dao.getCount() >= 30) {
            dao.deleteOldest()
        }

        val result = dao.insert(
            WatchedVideoEntity(
                title = title,
                watchUrl = videoId,
                views = videoViewCount,
                dateTime = videoDate,
                duration = duration,
                channelName = videoChannelName,
                channelThumbnail = channelThumbnail
            )
        )

        return result != -1L
    }

    suspend fun deleteWatchUrl(videoId: String): Int {
        return dao.deleteWatchUrl(videoId)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}