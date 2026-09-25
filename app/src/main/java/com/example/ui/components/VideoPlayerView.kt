package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.theme.XBlue
import com.example.ui.theme.XDarkSurfaceVariant

@Composable
fun VideoPlayerView(
    videoUrl: String,
    thumbnailUrl: String,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onPlayClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isBuffering by remember(videoUrl) { mutableStateOf(isPlaying) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .testTag("video_player_container"),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("native_video_view"),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val mediaController = MediaController(ctx)
                        mediaController.setAnchorView(this)
                        setMediaController(mediaController)
                        setVideoURI(Uri.parse(videoUrl))
                        setOnPreparedListener { mp ->
                            isBuffering = false
                            mp.isLooping = true
                            start()
                        }
                        setOnErrorListener { _, _, _ ->
                            isBuffering = false
                            false
                        }
                    }
                },
                update = { view ->
                    if (!view.isPlaying) {
                        view.start()
                    }
                }
            )

            if (isBuffering) {
                CircularProgressIndicator(
                    color = XBlue,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Close / stop player button
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close preview",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // Thumbnail with play button overlay
            if (thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(XDarkSurfaceVariant)
                )
            }

            // Subtle dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )

            // Circular Play Button
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(XBlue)
                    .testTag("play_video_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Video Preview",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Tap to Preview",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}
