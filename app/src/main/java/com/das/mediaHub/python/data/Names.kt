package com.das.mediaHub.python.data

enum class Names(val value: String) {

    /**
     * SearchWithLink is a function in Python that allows searching for video details using a YouTube URL.
     */
    SEARCH_WITH_URL("SearchWithLink"),

    /**
     * Searcher is a function in Python that allows searching for videos using keywords.
     */
    SEARCHER("Searcher"),

    /**
     * GET_VIDEO_STREAM_URL is a function in Python that allows fetching a video's stream URL.
     */
    GET_VIDEO_STREAM_URL("get_video_url"),

    /**
     * GET_AUDIO_STREAM_URL is a function in Python that allows fetching a video's audio stream URL.
     */
    GET_AUDIO_STREAM_URL("get_audio_url");
}