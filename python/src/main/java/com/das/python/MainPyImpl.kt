package com.das.python

import com.das.python.data.model.StreamUrlRespond
import com.das.python.data.interfaces.MainPy
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.model.responds.RespondVideoDetails
import com.das.python.data.model.responds.ResponseVideo

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
    override suspend fun getAudioStreamUrl(videoUrl: String): StreamUrlRespond =
        PyRuntime.callJson("main", "get_audio_url", videoUrl)

    /**
     * Fetches the direct video stream URL for a YouTube video.
     * Calls `main.get_video_url(videoUrl)`.
     */
    override suspend fun getVideoStreamUrl(videoUrl: String): StreamUrlRespond =
        PyRuntime.callJson("main", "get_video_url", videoUrl)

    /**
     * Retrieves all videos and metadata for a YouTube playlist.
     * Calls `main.get_playlist_url(playlistUrl)`.
     */
    override suspend fun getPlaylistUrl(playlistUrl: String): List<PlayListDataClass> =
        PyRuntime.callJson("main", "get_playlist_url", playlistUrl)

    /**
     * Performs a keyword search on YouTube.
     * Calls `main.Searcher(query)`.
     */
    override suspend fun searchNow(query: String): ResponseVideo =
        PyRuntime.callJson("main", "Searcher", query)

    /**
     * Retrieves full video details using a YouTube URL.
     * Calls `main.SearchWithLink(url)`.
     */
    override suspend fun searchByUrl(url: String): RespondVideoDetails =
        PyRuntime.callJson("main", "SearchWithLink", url)
}
