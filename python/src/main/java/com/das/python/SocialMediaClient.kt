package com.das.python

import com.das.python.PyRuntime.callJson
import com.das.python.data.Names
import com.das.python.data.model.Modules
import com.das.python.data.model.media.MediaInfo
import com.das.python.data.model.responds.ApiResponse

object SocialMediaClient {


    suspend fun getInstagramDetails(url: String): ApiResponse<MediaInfo> {
        return callJson(Modules.INSTAGRAM, function = Names.GET_INSTAGRAM_URL, url)
    }

    suspend fun resolveTikTokVideoUrl(ur: String):ApiResponse<MediaInfo> {
        return callJson(Modules.TIK_TOK, function = Names.GET_TIKTOK_URL, ur)
    }
}
