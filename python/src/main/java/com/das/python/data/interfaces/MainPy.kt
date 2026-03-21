package com.das.python.data.interfaces

import com.das.python.data.StreamUrlRespond
import com.das.python.data.annotation.PyFunction
import com.das.python.data.annotation.PyModule
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.model.responds.RespondVideoDetails
import com.das.python.data.model.responds.ResponseVideo

@PyModule("main")
interface MainPy {

    @PyFunction("get_audio_url")
    suspend fun getAudioStreamUrl(videoUrl: String): StreamUrlRespond

    @PyFunction("get_video_url")
    suspend fun getVideoStreamUrl(videoUrl: String): StreamUrlRespond

    @PyFunction("get_playlist_url")
    suspend fun getPlaylistUrl(playlistUrl: String): List<PlayListDataClass>

    @PyFunction("Searcher")
    suspend fun searchNow(query: String): ResponseVideo

    @PyFunction("SearchWithLink")
    suspend fun searchByUrl(url: String): RespondVideoDetails
}