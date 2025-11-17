package com.das.mediaHub.python

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.das.mediaHub.data.model.PlayListDataClass
import com.das.mediaHub.python.data.Names
import com.das.mediaHub.python.data.StreamUrlRespond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Handles interaction between the Android app and the embedded Python environment
 * using Chaquopy. This object helps start Python, call Python functions, and parse
 * their responses into Kotlin data classes.
 */
internal object PythonMain {
    val pythonInstant by lazy {
        Python.getInstance()
    }
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }


    /**
     * Starts the Python environment if it hasn’t been started yet.
     * Must be called once before running any Python code.
     */
    fun Application.startPython() {
        if (!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }
    }

    /**
     * Calls a Python function by name and passes arguments as a string.
     * Runs on the IO dispatcher to keep things off the main thread.
     *
     * @param name The name of the Python function to call.
     * @param args Arguments to pass to the Python function.
     * @return The result of the Python call as a string.
     */
    suspend inline fun <reified T> Python.callMethod(name: Names, args: String): T = withContext(Dispatchers.IO) {
        getModule("main")[name.value]?.call(args).toString().decodeStringToJson()
    }

    /**
     * Fetches a YouTube stream URL by calling a Python function that
     * processes the video link and returns stream info as JSON.
     *
     * @param type The type of Python function to call (e.g., "get_stream").
     * @param id The YouTube video ID.
     * @return A [StreamUrlRespond] object parsed from the JSON result.
     */
    suspend fun getStreamUrl(type: Names, id: String): StreamUrlRespond {

        return pythonInstant.callMethod(
            name = type,
            args = "https://www.youtube.com/watch?v=$id"
        )
    }

    suspend fun getPlayListUrl(url: String) : List<PlayListDataClass> {


        val python = pythonInstant.getModule("main")

        val getResultFromPython = withContext(Dispatchers.IO) {
            python["getPlayListUrls"]?.call(url)
        }.toString()

        return getResultFromPython.decodeStringToJson()
    }

    /**
     * Converts a JSON string into a Kotlin object of type [T].
     * Ignores unknown keys and coerces input values automatically.
     */
    inline fun <reified T> String.decodeStringToJson(): T {
        return jsonParser.decodeFromString<T>(this)
    }

}