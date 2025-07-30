package com.das.mediaHub.data.constants

import android.os.Bundle
import com.das.mediaHub.data.model.VideosListData

object GlobalVideoList {
    val listOfVideosListData = mutableListOf<VideosListData>()
    val previousVideosListData = mutableListOf<VideosListData>()
    val bundles = Bundle()
}