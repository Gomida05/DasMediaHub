package com.das.python

import com.das.python.data.StreamUrlRespond
import com.das.python.data.interfaces.MainPy
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.model.responds.RespondVideoDetails
import com.das.python.data.model.responds.ResponseVideo

class MainPyImpl : MainPy {
    override suspend fun getAudioStreamUrl(videoUrl: String): StreamUrlRespond =
        PyRuntime.callJson("main", "get_audio_url", videoUrl)

    override suspend fun getVideoStreamUrl(videoUrl: String): StreamUrlRespond =
        PyRuntime.callJson("main", "get_video_url", videoUrl)

    override suspend fun getPlaylistUrl(playlistUrl: String): List<PlayListDataClass> =
        PyRuntime.callJson("main", "get_playlist_url", playlistUrl)

    override suspend fun searchNow(query: String): ResponseVideo =
        PyRuntime.callJson("main", "Searcher", query)

    override suspend fun searchByUrl(url: String): RespondVideoDetails =
        PyRuntime.callJson("main", "SearchWithLink", url)
}