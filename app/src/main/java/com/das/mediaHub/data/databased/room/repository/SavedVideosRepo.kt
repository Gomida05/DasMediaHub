package com.das.mediaHub.data.databased.room.repository

import com.das.mediaHub.data.databased.room.dao.VideoDataDao
import com.das.mediaHub.data.databased.room.dataclass.VideoForRoom

class SavedVideosRepo(private val videoDataDao: VideoDataDao) {

    suspend fun getAllVideo(): List<VideoForRoom> {
        return videoDataDao.getAllVideos()
    }

    suspend fun insert(journey: VideoForRoom): Long {
        return videoDataDao.insert(journey)
    }

    suspend fun update(video: VideoForRoom){
        videoDataDao.updateVideo(video)
    }

    suspend fun getById(videoId: String): VideoForRoom? {
        return videoDataDao.searchById(videoId)
    }


    suspend fun delete(videoId: String) {
        videoDataDao.removeById(videoId)
    }


}