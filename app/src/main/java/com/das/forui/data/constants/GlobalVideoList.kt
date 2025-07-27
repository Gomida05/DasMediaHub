package com.das.forui.data.constants

import android.os.Bundle
import com.das.forui.data.model.VideosListData

object GlobalVideoList {
    val listOfVideosListData = mutableListOf<VideosListData>()
    val previousVideosListData = mutableListOf<VideosListData>()
    val bundles = Bundle()
}