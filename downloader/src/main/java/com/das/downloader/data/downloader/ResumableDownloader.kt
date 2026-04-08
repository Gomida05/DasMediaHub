package com.das.downloader.data.downloader

import com.das.downloader.data.model.download.DownloadTask
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

class ResumableDownloader(
    private val client: OkHttpClient = OkHttpClient()
) {

    sealed class Outcome {
        data object Completed : Outcome()
        data object Paused : Outcome()
        data object Canceled : Outcome()
        data class Failed(val message: String) : Outcome()
    }

    suspend fun download(
        task: DownloadTask,
        alreadyDownloadedBytes: Long,
        isPaused: () -> Boolean,
        isCanceled: () -> Boolean,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): Outcome = withContext(Dispatchers.IO) {
        val file = File(task.destinationPath)
        file.parentFile?.mkdirs()

        var startByte = alreadyDownloadedBytes
        if (file.exists() && file.length() != alreadyDownloadedBytes) {
            startByte = file.length()
        }

        try {
            val builder = Request.Builder().url(task.url)
            task.headers.forEach { (key, value) -> builder.addHeader(key, value) }

            if (startByte > 0L) {
                builder.addHeader("Range", "bytes=$startByte-")
            }

            client.newCall(builder.build()).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    return@withContext Outcome.Failed("HTTP ${response.code}")
                }

                val body = response.body

                val supportsResume = response.code == 206
                val contentLength = body.contentLength()

                val totalBytes = when {
                    supportsResume && contentLength >= 0 -> startByte + contentLength
                    !supportsResume && contentLength >= 0 -> contentLength
                    else -> -1L
                }

                if (startByte > 0 && !supportsResume) {
                    file.delete()
                    startByte = 0L
                }

                val raf = RandomAccessFile(file, "rw")
                if (startByte > 0L) {
                    raf.seek(startByte)
                } else {
                    raf.setLength(0L)
                }

                body.byteStream().use { input ->
                    raf.use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = startByte

                        while (true) {
                            ensureActive()

                            if (isCanceled()) {
                                output.close()
                                file.delete()
                                return@withContext Outcome.Canceled
                            }

                            if (isPaused()) {
                                return@withContext Outcome.Paused
                            }

                            val read = input.read(buffer)
                            if (read == -1) break

                            output.write(buffer, 0, read)
                            downloaded += read
                            onProgress(downloaded, totalBytes)
                        }
                    }
                }

                Outcome.Completed
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failed(e.message ?: "Unknown error")
        }
    }
}