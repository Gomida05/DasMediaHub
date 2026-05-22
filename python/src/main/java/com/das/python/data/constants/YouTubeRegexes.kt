package com.das.python.data.constants

/**
 * Constants and Regular Expressions used for validating and
 * parsing YouTube URLs.
 */
object YouTubeRegexes {
    /**
     * Regex pattern to extract an 11-character YouTube video ID.
     */
    const val YOUTUBE_REGEX = "(?<=v=|/)([a-zA-Z0-9_-]{11})(?=&|$|/)"

    /** Standard YouTube host with www. */
    const val YOUTUBE_HOST_1 = "www.youtube.com"

    /** Standard YouTube host without www. */
    const val YOUTUBE_HOST_2 = "youtube.com"

    /** Shortened YouTube host (youtu.be). */
    const val YOUTUBE_HOST_3 = "youtu.be"
}
