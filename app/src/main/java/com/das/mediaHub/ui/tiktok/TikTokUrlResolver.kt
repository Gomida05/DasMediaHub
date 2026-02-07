package com.das.mediaHub.ui.tiktok

import com.das.mediaHub.data.model.tiktok.ApiResponse
import com.das.mediaHub.data.model.tiktok.TikTokInfo
import com.das.mediaHub.python.PythonMain.callMethod
import com.das.mediaHub.python.PythonMain.pythonInstant
import com.das.mediaHub.python.data.Names

object TikTokUrlResolver {

    suspend fun resolveTikTokVideoUrl(tiktokUrl: String): ApiResponse<TikTokInfo> {
        val client = pythonInstant.callMethod<ApiResponse<TikTokInfo>>(name = Names.GET_TIKTOK_URL, args = tiktokUrl, module = "tiktok")
        return client

    }
}