package com.das.downloader.data.downloader

import com.das.downloader.data.model.Outcome
import com.das.downloader.data.model.download.DownloadTask
import com.das.downloader.exception.NetworkRequestException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.RandomAccessFile

/**
 * A low-level downloader that supports HTTP Range requests for resuming interrupted downloads.
 * 
 * It uses the Ktor [HttpClient] with the CIO engine for efficient, non-blocking I/O.
 * 
 * Example usage:
 * ```kotlin
 * val downloader = ResumableDownloader()
 * val outcome = downloader.download(
 *     task = myTask,
 *     alreadyDownloadedBytes = 1024L,
 *     isPaused = { false },
 *     isCanceled = { false },
 *     onProgress = { downloaded, total -> println("Progress: $downloaded/$total") }
 * )
 * ```
 */
class ResumableDownloader(
    private val client: HttpClient
) : Downloader {

    /**
     * Executes the download for the given [DownloadTask].
     * 
     * @param task The task metadata.
     * @param alreadyDownloadedBytes Bytes already saved to disk (for resuming).
     * @param isPaused Lambda to check if the task should be paused.
     * @param onProgress Callback to report download progress.
     * @return The [com.das.downloader.data.model.Outcome] of the download attempt.
     */
    override suspend fun download(
        task: DownloadTask,
        alreadyDownloadedBytes: Long,
        isPaused: () -> Boolean,
        onProgress: (downloaded: Long, total: Long) -> Unit
    ): Outcome {
        val file = File(task.destinationPath)
        file.parentFile?.mkdirs()

        var startByte = alreadyDownloadedBytes
        if (file.exists() && file.length() != alreadyDownloadedBytes) {
            startByte = file.length()
        }

        return try {
            client.prepareGet(task.url) {
                task.headers.forEach { (key, value) -> header(key, value) }
                if (startByte > 0L) {
                    header("Range", "bytes=$startByte-")
                }

                timeout {
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS // Large content can take a long time
                    connectTimeoutMillis = 15_000 // 15 seconds to connect
                    socketTimeoutMillis = 15_000  // 15 seconds between packets
                }
            }.execute { response ->

                if (response.status.value == 416) return@execute Outcome.Completed

                val statusCode = response.status.value
                val supportsResume = statusCode == 206
                val contentLength = response.headers["Content-Length"]?.toLongOrNull() ?: -1L

                val totalBytes = when {
                    supportsResume && contentLength >= 0 -> startByte + contentLength
                    !supportsResume && contentLength >= 0 -> contentLength
                    else -> -1L
                }

                if (startByte > 0 && !supportsResume) {
                    file.delete()
                    startByte = 0L
                }

                // Using standard Kotlin File appending/writing
                val raf = RandomAccessFile(file, "rw")
                if (startByte > 0L) {
                    raf.seek(startByte)
                } else {
                    raf.setLength(0L) // Ensure file is empty if we aren't resuming
                }

                val channel = response.bodyAsChannel()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = startByte

                var lastUpdateTime = System.currentTimeMillis()

                raf.use { output ->
                    while (true) {
                        // Check for manual pause (still valid since it's custom domain logic)
                        if (isPaused()) {
                            return@execute Outcome.Paused
                        }

                        val read = channel.readAvailable(buffer, 0, buffer.size)
                        if (read <= 0) {
                            if (channel.isClosedForRead) break
                            continue
                        }

                        output.write(buffer, 0, read)
                        downloaded += read

                        // 3. Throttle progress updates (e.g., every 100ms)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime > 100 || (totalBytes > 0 && downloaded == totalBytes)) {
                            onProgress(downloaded, totalBytes)
                            lastUpdateTime = currentTime
                        }
                    }
                }

                if (totalBytes > 0 && downloaded < totalBytes) {
                    return@execute Outcome.Failed("Incomplete download: $downloaded/$totalBytes bytes")
                }

                Outcome.Completed
            }
        } catch (_: CancellationException) {
            // 2. Cancellation caught here.
            // We delete the file because standard cancellation equals "Abort".
            // If they want to "Pause", they use the isPaused() lambda logic.
            file.delete()
            Outcome.Canceled
        } catch (e: HttpRequestTimeoutException) {
            Outcome.Failed("Request timed out: ${e.message}")
        } catch (e: NetworkRequestException) {
            Outcome.Failed("HTTP ${e.code}: ${e.message}")
        } catch (e: Exception) {
            Outcome.Failed(e.message ?: "Unknown error")
        }
    }
}
