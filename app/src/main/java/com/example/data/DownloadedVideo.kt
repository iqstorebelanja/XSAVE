package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_videos")
data class DownloadedVideo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val tweetId: String,
    val tweetUrl: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String,
    val tweetText: String,
    val qualityLabel: String,
    val resolution: String,
    val localFilePath: String,
    val originalVideoUrl: String,
    val thumbnailUrl: String,
    val fileSizeFormatted: String,
    val downloadedAt: Long = System.currentTimeMillis()
)
