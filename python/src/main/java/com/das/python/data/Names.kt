package com.das.python.data

/**
 * Enum class representing the names of Python functions
 * available in the embedded environment.
 *
 * Each entry maps a Kotlin constant to the actual function name
 * string used in the Python scripts.
 */
enum class Names(val value: String) {

    /**
     * SearchWithLink is a function in Python that allows searching for video details using a YouTube URL.
     */
    SEARCH_WITH_URL(value = "SearchWithLink"),

    /**
     * Searcher is a function in Python that allows searching for videos using keywords.
     */
    SEARCHER(value = "Searcher"),

    /**
     * GET_VIDEO_STREAM_URL is a function in Python that allows fetching a video's stream URL.
     */
    GET_VIDEO_STREAM_URL(value = "get_video_url"),

    /**
     * GET_AUDIO_STREAM_URL is a function in Python that allows fetching a video's audio stream URL.
     */
    GET_AUDIO_STREAM_URL(value = "get_audio_url"),

    /**
     * GET_PLAYLIST_URL is a function in Python that allows fetching a playlist's URL.
     */
    GET_PLAYLIST_URL(value = "get_playlist_url"),

    /**
     * GET_TIKTOK_URL is a function in Python that allows fetching a video's stream URL.
     */
    GET_TIKTOK_URL(value = "get_video_stream");
}
