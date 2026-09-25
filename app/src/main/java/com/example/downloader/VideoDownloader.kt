package com.example.downloader

import android.content.Context
import android.os.Environment
import com.example.data.AppDatabase
import com.example.data.DownloadedVideo
import com.example.model.TweetData
import com.example.model.VideoVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class DownloadState {
    object Idle : DownloadState()
    data class Progress(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    data class Success(val downloadedVideo: DownloadedVideo) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object VideoDownloader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun downloadVideo(
        context: Context,
        tweet: TweetData,
        variant: VideoVariant
    ): Flow<DownloadState> = flow {
        emit(DownloadState.Progress(0, 0, 0))

        try {
            // Target directory: app's external movies / downloads directory (no dangerous permissions needed)
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                ?: context.filesDir

            val xsaveFolder = File(baseDir, "XSave")
            if (!xsaveFolder.exists()) {
                xsaveFolder.mkdirs()
            }

            // Clean sanitized filename
            val safeAuthor = tweet.authorUsername.replace(Regex("[^a-zA-Z0-9_]"), "")
            val safeQuality = variant.qualityLabel.substringBefore(" ").replace(" ", "_")
            val fileName = "XSave_${safeAuthor}_${tweet.id}_${safeQuality}_${System.currentTimeMillis()}.mp4"
            val targetFile = File(xsaveFolder, fileName)

            val request = Request.Builder()
                .url(variant.url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                emit(DownloadState.Error("HTTP Error: ${response.code} ${response.message}"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(DownloadState.Error("Empty response from media server"))
                return@flow
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L
            val buffer = ByteArray(8192)

            body.byteStream().use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    var bytesRead: Int
                    var lastReportedPercent = 0

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (totalBytes > 0) {
                            val percent = ((downloadedBytes * 100) / totalBytes).toInt()
                            if (percent > lastReportedPercent) {
                                lastReportedPercent = percent
                                emit(DownloadState.Progress(percent, downloadedBytes, totalBytes))
                            }
                        } else {
                            emit(DownloadState.Progress(50, downloadedBytes, 0))
                        }
                    }
                    outputStream.flush()
                }
            }

            val formattedSize = formatFileSize(targetFile.length())

            val downloadedEntity = DownloadedVideo(
                tweetId = tweet.id,
                tweetUrl = tweet.originalUrl,
                authorName = tweet.authorName,
                authorUsername = tweet.authorUsername,
                authorAvatarUrl = tweet.authorAvatarUrl,
                tweetText = tweet.text,
                qualityLabel = variant.qualityLabel,
                resolution = variant.resolution,
                localFilePath = targetFile.absolutePath,
                originalVideoUrl = variant.url,
                thumbnailUrl = tweet.videos.firstOrNull()?.thumbnailUrl ?: "",
                fileSizeFormatted = formattedSize,
                downloadedAt = System.currentTimeMillis()
            )

            // Save to Room database
            val dao = AppDatabase.getInstance(context).downloadedVideoDao()
            dao.insert(downloadedEntity)

            emit(DownloadState.Success(downloadedEntity))

        } catch (e: Exception) {
            emit(DownloadState.Error(e.localizedMessage ?: "Failed to download video"))
        }
    }.flowOn(Dispatchers.IO)

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb >= 1000) {
            val gb = mb / 1024
            String.format(Locale.US, "%.2f GB", gb)
        } else {
            String.format(Locale.US, "%.1f MB", mb)
        }
    }
}
