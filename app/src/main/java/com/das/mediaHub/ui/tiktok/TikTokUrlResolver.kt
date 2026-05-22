package com.das.mediaHub.ui.tiktok

import com.das.mediaHub.data.model.tiktok.ApiResponse
import com.das.mediaHub.data.model.tiktok.TikTokInfo
import com.das.python.PythonMain.callMethod
import com.das.python.PythonMain.pythonInstant
import com.das.python.data.Names
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TikTokUrlResolver {

    suspend fun resolveTikTokVideoUrl(tiktokUrl: String): ApiResponse<TikTokInfo>  = withContext(Dispatchers.IO) {
        val client = pythonInstant.callMethod<ApiResponse<TikTokInfo>>(name = Names.GET_TIKTOK_URL, args = tiktokUrl, module = "tiktok")
        client
    }
}