package com.das.python

import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.das.python.exceptions.PyCallError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

object PyRuntime {

    private val moduleCache = ConcurrentHashMap<String, PyObject>()
    private val funcCache = ConcurrentHashMap<String, PyObject>()

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun ensureStarted() {
        if (!Python.isStarted()) error("Python is not started. Call Application.startPython() first.")
    }

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

    private fun function(module: String, name: String): PyObject {
        val key = "$module:$name"
        return funcCache.getOrPut(key) {
            val m = module(module)
            m[name] ?: throw PyCallError.FunctionNotFound(module, name)
        }
    }

    internal suspend inline fun <reified T> callJson(
        module: String,
        function: String,
        vararg args: Any?
    ): T = withContext(Dispatchers.IO) {
        ensureStarted()
        try {
            val f = function(module, function)
            val result = f.call(*args) ?: throw Exception("Python returned null from $module.$function")
            json.decodeFromString(result.toString())
        } catch (e: PyException) {
            e.printStackTrace()
            throw RuntimeException("Python crashed in $module.$function", e)
        }
    }
}