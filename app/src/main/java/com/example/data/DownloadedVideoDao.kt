package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedVideoDao {
    @Query("SELECT * FROM downloaded_videos ORDER BY downloadedAt DESC")
    fun getAllVideos(): Flow<List<DownloadedVideo>>

    @Query("SELECT * FROM downloaded_videos WHERE tweetId = :tweetId LIMIT 1")
    suspend fun getVideoByTweetId(tweetId: String): DownloadedVideo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: DownloadedVideo): Long

    @Query("DELETE FROM downloaded_videos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloaded_videos")
    suspend fun deleteAll()
}
