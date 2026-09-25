package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.DownloadedVideo
import com.example.ui.MainViewModel
import com.example.ui.components.LinearGridBackground
import com.example.ui.components.MoreAppsFooterSection
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Media Vault Screen:
 * Styled like iOS Photos grid:
 * - 2-column sleek photo/video card grid
 * - Hover play icon overlay with video length/quality pill
 * - Long press to delete with confirmation dialog
 * - Pure #000000 background with subtle grid pattern
 * - Linear/Raycast glassmorphism tokens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    videos: List<DownloadedVideo>,
    onNavigateToDownloader: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedVideoForPlayback by remember { mutableStateOf<DownloadedVideo?>(null) }
    var videoToDelete by remember { mutableStateOf<DownloadedVideo?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val filteredVideos = remember(videos, searchQuery) {
        if (searchQuery.isBlank()) {
            videos
        } else {
            videos.filter {
                it.tweetText.contains(searchQuery, ignoreCase = true) ||
                        it.authorName.contains(searchQuery, ignoreCase = true) ||
                        it.authorUsername.contains(searchQuery, ignoreCase = true) ||
                        it.qualityLabel.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LinearGridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Vault Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Media Vault",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPureWhite,
                            letterSpacing = (-0.5).sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x1800D1FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${videos.size} items",
                                color = NeonBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "iOS Photos style offline storage • Long press to manage",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (videos.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GlassSurfaceElevated)
                            .testTag("clear_all_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Vault",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar (Glassmorphic)
            if (videos.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter saved videos...", color = TextSecondary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassSurfaceElevated,
                        unfocusedContainerColor = GlassSurface,
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPureWhite,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_search_field")
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // iOS Photos Grid
            if (filteredVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                            .padding(28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0x1800D1FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "No Matching Media" else "Media Vault is Empty",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPureWhite
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different query or author" else "Downloaded videos in 1080p or 4K will appear here automatically",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onNavigateToDownloader,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_state_go_download_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = PureBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Videos", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredVideos, key = { it.id }) { video ->
                        IosPhotoGridCard(
                            video = video,
                            onPlay = { selectedVideoForPlayback = video },
                            onLongPress = { videoToDelete = video },
                            onShare = { shareVideo(context, video) }
                        )
                    }

                    // Bottom Cross Promo Section
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(10.dp))
                        MoreAppsFooterSection()
                    }
                }
            }
        }

        // In-app Video Playback Dialog
        selectedVideoForPlayback?.let { video ->
            Dialog(
                onDismissRequest = { selectedVideoForPlayback = null }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = XDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.authorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPureWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "@${video.authorUsername} • ${video.qualityLabel}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            IconButton(onClick = { selectedVideoForPlayback = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Video Player Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        val mediaController = MediaController(ctx)
                                        mediaController.setAnchorView(this)
                                        setMediaController(mediaController)
                                        setVideoPath(video.localFilePath)
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            start()
                                        }
                                        setOnErrorListener { _, _, _ ->
                                            Toast.makeText(ctx, "Playback error", Toast.LENGTH_SHORT).show()
                                            true
                                        }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { shareVideo(context, video) },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", color = TextPrimary, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { openVideoInExternalPlayer(context, video) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = PureBlack, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("External Player", color = PureBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Long-Press Delete Confirmation Dialog
        videoToDelete?.let { video ->
            AlertDialog(
                onDismissRequest = { videoToDelete = null },
                containerColor = XDarkSurface,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text("Delete Video?", color = TextPureWhite, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Are you sure you want to permanently delete this video by @${video.authorUsername}?",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteDownloadedVideo(video)
                            videoToDelete = null
                            Toast.makeText(context, "Deleted from vault", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = XRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { videoToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Clear All Dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                containerColor = XDarkSurface,
                shape = RoundedCornerShape(16.dp),
                title = {
                    Text("Clear All Videos?", color = TextPureWhite, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "This will delete all ${videos.size} offline video files permanently from your vault.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllDownloads()
                            showDeleteAllDialog = false
                            Toast.makeText(context, "All media cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = XRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

/**
 * iOS Photos Grid Card:
 * Aspect ratio tile with subtle gradient shadow, hover play icon overlay, quality badge,
 * and long-press trigger for delete action.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IosPhotoGridCard(
    video: DownloadedVideo,
    onPlay: () -> Unit,
    onLongPress: () -> Unit,
    onShare: () -> Unit
) {
    val formattedDate = remember(video.downloadedAt) {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(video.downloadedAt))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GlassSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onPlay,
                onLongClick = onLongPress
            )
            .testTag("vault_card_${video.id}")
    ) {
        // Thumbnail / Poster
        if (video.thumbnailUrl.isNotBlank()) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.tweetText,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF101216)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Dark gradient bottom overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color(0xCC000000)
                        )
                    )
                )
        )

        // Center Hover Play Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0x99000000))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = NeonBlue,
                modifier = Modifier.size(24.dp)
            )
        }

        // Top Badges (Quality & File size)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xB3000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.qualityLabel.substringBefore(" "),
                    color = NeonBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xB3000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.fileSizeFormatted,
                    color = TextPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom Details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = "@${video.authorUsername}",
                color = TextPureWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formattedDate,
                color = TextSecondary,
                fontSize = 9.sp
            )
        }
    }
}

private fun openVideoInExternalPlayer(context: Context, video: DownloadedVideo) {
    try {
        val file = File(video.localFilePath)
        if (!file.exists()) {
            Toast.makeText(context, "Video file not found", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/mp4")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open video: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareVideo(context: Context, video: DownloadedVideo) {
    try {
        val file = File(video.localFilePath)
        if (!file.exists()) {
            Toast.makeText(context, "Video file not found", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Downloaded via ALLVID: ${video.tweetText.take(100)}...")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Video via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
