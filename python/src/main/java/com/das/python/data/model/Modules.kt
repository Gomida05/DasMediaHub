package com.das.python.data.model

/**
 * Enumeration of available Python modules in the project.
 *
 * Each constant maps to a specific `.py` file within the Python source directory,
 * allowing the [com.das.python.PyRuntime] to identify and load the correct module
 * for function execution.
 *
 * @property value The actual name of the Python file (without extension).
 */
enum class Modules(val value: String) {

    /**
     * The core module (`main.py`) handling YouTube-specific searches, 
     * stream extraction using pytubefix, and common helper utilities.
     */
    MAIN(value = "main"),

    /**
     * The social media extraction module (`UrlExtractor.py`) powered by yt-dlp.
     * Used for retrieving metadata and stream URLs from platforms like 
     * TikTok, Instagram, and generic video links.
     */
    SOCIAL_MEDIA(value = "UrlExtractor")
}
