package com.das.python

import com.das.python.PyRuntime.callJson
import com.das.python.data.Names
import com.das.python.data.model.Modules
import com.das.python.data.model.media.MediaInfo
import com.das.python.data.model.responds.ApiResponse

/**
 * Client for interacting with social media platforms (TikTok, Instagram, YouTube) via a 
 * Python-powered extraction engine.
 *
 * This object provides high-level methods to extract media metadata and stream URLs using `yt-dlp`,
 * wrapped in the [Modules.SOCIAL_MEDIA] (UrlExtractor) Python module.
 */
object SocialMediaClient {

    /**
     * Retrieves detailed media information and a progressive MP4 stream URL for a given social media link.
     *
     * This method invokes the `UrlExtractor.getInfoFromUrl` Python function to scrape metadata including
     * the video ID, title, duration, uploader, and engagement statistics (views/likes), alongside
     * the best available progressive MP4 stream.
     *
     * @param url The full URL of the social media content (e.g., TikTok, Instagram, YouTube).
     * @return An [ApiResponse] containing the [MediaInfo] if successful, or a descriptive error
     * message if extraction fails or content is unavailable.
     */
    suspend fun getUrlInfo(url: String): ApiResponse<MediaInfo> {
        return callJson(Modules.SOCIAL_MEDIA, function = Names.GET_URL_EXTRACTOR, url)
    }
}