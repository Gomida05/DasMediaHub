package com.das.python

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.das.python.data.Names
import com.das.python.data.model.PlayListDataClass
import com.das.python.data.StreamUrlRespond
import com.das.python.data.annotation.PythonModule
import com.das.python.data.annotation.RequiresPythonInit
import com.das.python.data.model.ensurePythonInitialized
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Bridge object responsible for communication between
 * the Android layer and the embedded Python runtime (Chaquopy).
 *
 * Responsibilities:
 * - Start Python runtime
 * - Invoke Python functions
 * - Convert JSON results into Kotlin models
 *
 * All Python calls are executed on [Dispatchers.IO].
 */
object PythonMain {

    /**
     * Lazy instance of the embedded Python interpreter.
     *
     * Accessing this before [Application.startPython]
     * may cause runtime failure.
     */
    @get:RequiresPythonInit
    val pythonInstant by lazy {
        Python.getInstance()
    }

    /**
     * JSON parser used to deserialize Python responses.
     *
     * Configuration:
     * - ignoreUnknownKeys = true
     * - coerceInputValues = true
     */
    val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Starts the embedded Python environment.
     *
     * Must be called once (typically inside Application.onCreate()).
     *
     * Safe to call multiple times — Python will only start once.
     *
     * @receiver Application instance
     *
     * @throws IllegalStateException if Python fails to initialize
     */
    @Throws(IllegalStateException::class)
    fun Application.startPython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }

    /**
     * Calls a Python function inside the specified module.
     *
     * The result from Python must be a JSON string which will
     * be deserialized into type [T].
     *
     * This function runs on Dispatchers.IO.
     *
     * @param name Enum representing the Python function name.
     * @param args Argument string passed to the Python function.
     * @param module Python module name (default = "main").
     *
     * @return Parsed Kotlin object of type [T].
     *
     * @throws IllegalStateException If Python runtime is not started.
     * @throws NullPointerException If module or method is not found.
     * @throws SerializationException If JSON parsing fails.
     * @throws Exception Any exception thrown from the Python layer.
     */
    @Throws(
        IllegalStateException::class,
        NullPointerException::class,
        SerializationException::class,
        Exception::class
    )
    @RequiresPythonInit
    inline fun <reified T> Python.callMethod(
        name: Names,
        args: String,
        @PythonModule module: String = "main"
    ): T  {

        ensurePythonInitialized(Python.isStarted())
        val pyModule = getModule(module)
        val result = pyModule[name.value]
            ?: throw NullPointerException("Python method '${name.value}' not found in module '$module'")

        val callResult = result.call(args)
            ?: throw NullPointerException("Python call returned null")

        return callResult.toString().decodeStringToJson()
    }

    /**
     * Retrieves a stream URL (audio/video) for a YouTube video.
     *
     * Internally calls a Python function that extracts
     * streaming information and returns it as JSON.
     *
     * @param type Python function identifier.
     * @param id YouTube video ID.
     *
     * @return Parsed [StreamUrlRespond] object.
     *
     * @throws IllegalStateException If Python is not started.
     * @throws SerializationException If JSON response is malformed.
     * @throws Exception If Python execution fails.
     */
    @Throws(
        IllegalStateException::class,
        SerializationException::class,
        Exception::class
    )
    @RequiresPythonInit
    suspend fun getStreamUrl(
        type: Names,
        id: String,
        @PythonModule module: String = "main"
    ): StreamUrlRespond {

        return pythonInstant.callMethod(
            name = type,
            args = "https://www.youtube.com/watch?v=$id",
            module = module
        )
    }

    /**
     * Retrieves playlist data from a YouTube playlist URL.
     *
     * @param url Full YouTube playlist URL.
     *
     * @return List of [PlayListDataClass].
     *
     * @throws IllegalStateException If Python is not started.
     * @throws SerializationException If JSON parsing fails.
     * @throws Exception If Python execution fails.
     */
    @Throws(
        IllegalStateException::class,
        SerializationException::class,
        Exception::class
    )
    suspend fun getPlayListUrl(
        url: String
    ): List<PlayListDataClass> {

        return pythonInstant.callMethod(
            name = Names.GET_PLAYLIST_URL,
            args = url
        )
    }

    /**
     * Decodes a JSON string into a Kotlin object of type [T].
     *
     * Uses the configured [jsonParser].
     *
     * @receiver JSON string.
     *
     * @throws SerializationException If JSON is invalid
     * or does not match the target type.
     */
    @Throws(SerializationException::class)
    inline fun <reified T> String.decodeStringToJson(): T {
        return jsonParser.decodeFromString(this)
    }
}