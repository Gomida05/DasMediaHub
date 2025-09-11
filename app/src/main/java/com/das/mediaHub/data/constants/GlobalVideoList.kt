package com.das.mediaHub.data.constants

import android.os.Bundle
import com.das.mediaHub.data.model.VideosListData
import com.das.mediaHub.data.model.searcher.Video

object GlobalVideoList {
    val listOfVideosListData = mutableListOf<Video>()
    val previousVideosListData = mutableListOf<Video>()
    val bundles = Bundle()
}