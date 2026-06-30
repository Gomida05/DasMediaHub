package com.das.python

import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.das.python.data.Names
import com.das.python.data.model.Modules
import com.das.python.exceptions.PyCallError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/**
 * Internal runtime manager for Python module and function caching.
 *
 * This object provides a high-level API for calling Python functions
 * that return JSON data, handling serialization and threading.
 *
 * Example:
 * ```
 * val result: MyModel = PyRuntime.callJson("my_module", "my_function", "arg1", 123)
 * ```
 */
object PyRuntime {

    /**
     * Cache for loaded Python modules to avoid repeated lookups.
     */
    private val moduleCache = ConcurrentHashMap<String, PyObject>()

    /**
     * Cache for Python function references.
     */
    private val funcCache = ConcurrentHashMap<String, PyObject>()

    /**
     * Default JSON configuration used for decoding Python responses.
     */
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Ensures that the Python runtime has been started.
     * @throws IllegalStateException if Python is not started.
     */
    private fun ensureStarted() {
        if (!Python.isStarted()) error("Python is not started. Call Application.startPython() first.")
    }

    /**
     * Retrieves a Python module by name, using cache if available.
     *
     * @param name Module name.
     * @return [PyObject] representing the module.
     * @throws PyCallError.PythonException if the module cannot be loaded.
     */
    private fun module(name: String): PyObject {
        return moduleCache.getOrPut(name) {
            try {
                PythonMain.pythonInstant.getModule(name)
            } catch (e: Exception) {
                e.printStackTrace()
                throw PyCallError.PythonException(e)
            }
        }
    }

    /**
     * Retrieves a Python function from a module, using cache if available.
     *
     * @param module Module name.
     * @param name Function name.
     * @return [PyObject] representing the function.
     * @throws PyCallError.FunctionNotFound if the function is missing.
     */
    private fun function(module: String, name: String): PyObject {
        val key = "$module:$name"
        return funcCache.getOrPut(key) {
            val m = module(module)
            m[name] ?: throw PyCallError.FunctionNotFound(module, name)
        }
    }

    /**
     * Calls a Python function and decodes its JSON result into [T].
     *
     * This function switches to [Dispatchers.IO] context.
     *
     * @param module Python module name.
     * @param function Python function name.
     * @param args Arguments to pass to the function.
     * @return Decoded object of type [T].
     *
     * @throws RuntimeException if Python crashes or returns null.
     */
    internal suspend inline fun <reified T> callJson(
        module: Modules,
        function: Names,
        vararg args: Any?
    ): T = withContext(Dispatchers.IO) {
        ensureStarted()
        try {
            val f = function(module.value, function.value)
            val result = f.call(*args) ?: throw Exception("Python returned null from $module.$function")
            json.decodeFromString(result.toString())
        } catch (e: PyException) {
            e.printStackTrace()
            throw RuntimeException("Python crashed in $module.$function", e)
        }
    }
}
