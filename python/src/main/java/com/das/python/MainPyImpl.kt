package com.das.python

import com.das.python.data.Names
import com.das.python.data.interfaces.MainPy
import com.das.python.data.model.FewVideoDetails
import com.das.python.data.model.Modules.MAIN
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.model.responds.ApiResponse
import com.das.python.data.model.searcher.SearchResponse

/**
 * Implementation of [MainPy] interface that bridges Kotlin calls
 * to Python functions defined in the `main.py` module.
 *
 * All methods are suspendable and execute on the IO dispatcher
 * via [PyRuntime].
 */
internal class MainPyImpl : MainPy {

    /**
     * Fetches the direct audio stream URL for a YouTube video.
     * Calls `main.get_audio_url(videoUrl)`.
     */
    override suspend fun getAudioStreamUrl(videoUrl: String): ApiResponse<String> =
        PyRuntime.callJson(MAIN, Names.GET_AUDIO_STREAM_URL, videoUrl)

    /**
     * Fetches the direct video stream URL for a YouTube video.
     * Calls `main.get_video_url(videoUrl)`.
     */
    override suspend fun getVideoStreamUrl(videoUrl: String): ApiResponse<String> =
        PyRuntime.callJson(MAIN, Names.GET_VIDEO_STREAM_URL, videoUrl)

    /**
     * Retrieves all videos and metadata for a YouTube playlist.
     * Calls `main.get_playlist_url(playlistUrl)`.
     */
    override suspend fun getPlaylistUrl(playlistUrl: String): List<PlayListDataClass> =
        PyRuntime.callJson(MAIN, Names.GET_PLAYLIST_URL, playlistUrl)

    /**
     * Performs a keyword search on YouTube.
     * Calls `main.Searcher(query)`.
     */
    override suspend fun searchNow(query: String): ApiResponse<SearchResponse> =
        PyRuntime.callJson(MAIN, Names.SEARCHER, query)

    /**
     * Retrieves full video details using a YouTube URL.
     * Calls `main.SearchWithLink(url)`.
     */
    override suspend fun searchByUrl(url: String): ApiResponse<FewVideoDetails> =
        PyRuntime.callJson(MAIN, Names.SEARCH_WITH_URL, url)
}
