package com.das.python.data.interfaces

import com.das.python.data.annotation.PyFunction
import com.das.python.data.annotation.PyModule
import com.das.python.data.model.FewVideoDetails
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.model.responds.ApiResponse
import com.das.python.data.model.searcher.SearchResponse

/**
 * Interface representing the core Python operations available
 * in the `main.py` module.
 *
 * This interface is used to abstract the communication with Python.
 * Use [com.das.python.MainPyImpl] for the concrete implementation.
 */
@PyModule("main")
interface MainPy {

    /**
     * Extracts the direct audio stream URL for a given YouTube video.
     */
    @PyFunction("get_audio_url")
    suspend fun getAudioStreamUrl(videoUrl: String): ApiResponse<String>

    /**
     * Extracts the direct video stream URL for a given YouTube video.
     */
    @PyFunction("get_video_url")
    suspend fun getVideoStreamUrl(videoUrl: String): ApiResponse<String>

    /**
     * Retrieves metadata for all videos within a YouTube playlist.
     */
    @PyFunction("get_playlist_url")
    suspend fun getPlaylistUrl(playlistUrl: String): List<PlayListDataClass>

    /**
     * Searches YouTube for videos matching the provided query string.
     */
    @PyFunction("Searcher")
    suspend fun searchNow(query: String): ApiResponse<SearchResponse>

    /**
     * Retrieves comprehensive metadata for a single video via its URL.
     */
    @PyFunction("SearchWithLink")
    suspend fun searchByUrl(url: String): ApiResponse<FewVideoDetails>
}
