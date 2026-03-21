package com.das.mediaHub

import com.das.python.YouTuber.formatDate
import com.das.python.YouTuber.formatViews
import com.das.python.YouTuber.isValidYoutubeURL
import com.das.python.YouTuber.isValidYouTubePlaylistUrl
import com.das.python.YouTuber.youtubeExtractor
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun youtubeExtractor_isCorrect() {
        val url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val id = url.youtubeExtractor()
        assertEquals("dQw4w9WgXcQ", id)
    }

    @Test
    fun youtubeUrlValidation_isCorrect() {
        val valid = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        val invalid = "https://google.com"

        assertTrue(valid.isValidYoutubeURL())
        assertFalse(invalid.isValidYoutubeURL())
    }

    @Test
    fun playlistValidation_isCorrect() {
        val playlistUrl =
            "https://www.youtube.com/playlist?list=PL1234567890ABCDE"
        assertTrue(playlistUrl.isValidYouTubePlaylistUrl())
    }

    @Test
    fun formatViews_isCorrect() {
        assertEquals("1.2K", "1200".formatViews())
        assertEquals("1.2M", "1200000".formatViews())
        assertEquals("1.2B", "1200000000".formatViews())
    }

    @Test
    fun formatDate_isCorrect() {
        val input = "2024-01-15"
        val formatted = input.formatDate()
        assertEquals(input, formatted)
    }
}