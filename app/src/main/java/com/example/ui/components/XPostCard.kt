package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.TweetData
import com.example.model.VideoMedia
import com.example.ui.theme.*

/**
 * Authentic X.com (Twitter) Post Card:
 * - OLED Black background with XCardBorder
 * - Author avatar, display name, verified badge, @handle, timestamp, 𝕏 logo
 * - Tweet text with high readability
 * - Multi-video tabs switcher when tweet has multiple videos
 * - In-post video preview player
 * - X Engagement Metrics: Replies, Reposts, Likes, Bookmark, Share
 */
@Composable
fun XPostCard(
    tweet: TweetData,
    selectedMediaIndex: Int,
    onSelectMediaIndex: (Int) -> Unit,
    isPlaying: Boolean,
    activePlayingUrl: String?,
    previewUrl: String,
    onPlayClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeVideo = tweet.videos.getOrNull(selectedMediaIndex) ?: tweet.videos.firstOrNull()

    Surface(
        color = XDarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, XCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("x_tweet_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. Author Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                if (tweet.authorAvatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = tweet.authorAvatarUrl,
                        contentDescription = tweet.authorName,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.dp, XCardBorder, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(XDarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tweet.authorName.take(1).uppercase(),
                            color = TextPureWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Author Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tweet.authorName,
                            color = TextPureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (tweet.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Account",
                                tint = XBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${tweet.authorUsername}",
                            color = XTextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = " · ",
                            color = XTextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = tweet.createdAt.ifBlank { "Recent" },
                            color = XTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                // X icon & Open in X action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "𝕏",
                        color = TextPureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweet.originalUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Original Post on X",
                            tint = XTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Tweet Body Text
            Text(
                text = tweet.text,
                color = XTextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Multi-Video Selector (If Tweet contains multiple videos)
            if (tweet.videos.size > 1) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "Extracted Videos (${tweet.videos.size})",
                        color = XBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tweet.videos.forEachIndexed { index, video ->
                            val isSelected = index == selectedMediaIndex
                            val maxQuality = video.variants.firstOrNull()?.qualityLabel ?: "Video"

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0x331D9BF0) else XDarkSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) XBlue else XCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectMediaIndex(index) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                                    .testTag("video_tab_$index")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = if (isSelected) XBlue else XTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Video ${index + 1} ($maxQuality)",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) XBlue else XTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. In-App Video Player Preview
            activeVideo?.let { video ->
                VideoPlayerView(
                    videoUrl = previewUrl,
                    thumbnailUrl = video.thumbnailUrl,
                    isPlaying = isPlaying && activePlayingUrl == previewUrl,
                    onPlayClick = onPlayClick,
                    onCloseClick = onCloseClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Authentic X Engagement Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = XCardBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    count = formatMetricCount(tweet.repliesCount),
                    contentDesc = "Replies"
                )
                MetricItem(
                    icon = Icons.Default.Repeat,
                    count = formatMetricCount(tweet.retweetsCount),
                    contentDesc = "Reposts"
                )
                MetricItem(
                    icon = Icons.Default.FavoriteBorder,
                    count = formatMetricCount(tweet.likesCount),
                    contentDesc = "Likes",
                    tintHover = XPink
                )
                MetricItem(
                    icon = Icons.Default.BookmarkBorder,
                    count = "Save",
                    contentDesc = "Bookmark"
                )
                MetricItem(
                    icon = Icons.Default.Share,
                    count = "Share",
                    contentDesc = "Share"
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    count: String,
    contentDesc: String,
    tintHover: Color = XBlue
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = XTextSecondary,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = count,
            color = XTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatMetricCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        count > 0 -> count.toString()
        else -> "0"
    }
}
