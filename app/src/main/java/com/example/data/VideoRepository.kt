package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class VideoRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).downloadedVideoDao()

    val allVideos: Flow<List<DownloadedVideo>> = dao.getAllVideos()

    suspend fun insert(video: DownloadedVideo): Long = withContext(Dispatchers.IO) {
        dao.insert(video)
    }

    suspend fun delete(video: DownloadedVideo) = withContext(Dispatchers.IO) {
        // Also remove local file
        try {
            val file = File(video.localFilePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}

        dao.deleteById(video.id)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}
