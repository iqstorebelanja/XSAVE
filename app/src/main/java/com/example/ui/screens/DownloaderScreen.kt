package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ads.AdsConfig
import com.example.downloader.DownloadState
import com.example.model.VideoVariant
import com.example.network.TwitterExtractor
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

/**
 * DownloaderScreen:
 * - X.com (Twitter) OLED Black Theme
 * - Clean, fast, mobile-first PWA design
 * - Extract all videos from tweet (multiple qualities)
 * - Show tweet text + author + verified status
 * - Download HD (1080p), 720p, 480p, 4K
 * - Uses QualityButton component with AdMob / ads.ts logic
 */
@Composable
fun DownloaderScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    is1080pUnlocked: Boolean,
    is4KUnlocked: Boolean,
    daily720pCount: Int,
    adsWatchedFor4K: Int,
    onNavigateToLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val samples = remember { TwitterExtractor.getAllSampleTweets() }

    Surface(
        color = XBlack,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Banner & Quality Gate Status Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "𝕏 XSave Downloader",
                                color = TextPureWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.3).sp
                            )
                        }
                        Text(
                            text = "720p Free (3x/day) • 1080p 1 Ad • 4K 2 Ads",
                            color = XTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Quality Gate Status Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // 720p Limit Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (daily720pCount < AdsConfig.FREE_720P_DAILY_LIMIT) Color(0x2200BA7C) else XDarkSurface)
                                .border(1.dp, if (daily720pCount < AdsConfig.FREE_720P_DAILY_LIMIT) XGreen else XCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "720p: ${daily720pCount}/${AdsConfig.FREE_720P_DAILY_LIMIT}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (daily720pCount < AdsConfig.FREE_720P_DAILY_LIMIT) XGreen else XTextSecondary
                            )
                        }

                        // 1080p Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (is1080pUnlocked) Color(0x2200BA7C) else XDarkSurface)
                                .border(1.dp, if (is1080pUnlocked) XGreen else XCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (is1080pUnlocked) "1080p (24h)" else "1080p 1 Ad",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (is1080pUnlocked) XGreen else XTextSecondary
                            )
                        }

                        // 4K Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (is4KUnlocked) Color(0x221D9BF0) else XDarkSurface)
                                .border(1.dp, if (is4KUnlocked) XBlue else XCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (is4KUnlocked) "4K (24h)" else "4K 2 Ads",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (is4KUnlocked) XBlue else XTextSecondary
                            )
                        }
                    }
                }
            }

            // 2. Mobile-first X URL Input Bar
            item {
                XUrlInputBar(
                    value = uiState.urlInput,
                    onValueChange = { viewModel.onUrlChanged(it) },
                    onSubmit = { viewModel.extractTweet() },
                    onClear = { viewModel.clearUrl() },
                    isLoading = uiState.isLoading
                )
            }

            // 3. Quick Demo Presets (Including Multi-Video Tweet)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Presets:",
                        fontSize = 12.sp,
                        color = XTextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    samples.forEach { sample ->
                        val isCurrent = uiState.currentTweet?.id == sample.id
                        val videoCountBadge = if (sample.videos.size > 1) " (${sample.videos.size} videos)" else ""

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isCurrent) XDarkSurfaceElevated else XDarkSurface)
                                .border(
                                    1.dp,
                                    if (isCurrent) XBlue else XCardBorder,
                                    CircleShape
                                )
                                .clickable { viewModel.loadSampleTweet(sample.id) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("demo_chip_${sample.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "@${sample.authorUsername}$videoCountBadge",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) XBlue else XTextPrimary
                            )
                        }
                    }
                }
            }

            // 4. Status message toast banner
            uiState.statusMessage?.let { status ->
                item {
                    Surface(
                        color = XDarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300BA7C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = status,
                                color = XGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { viewModel.clearStatusMessage() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = XTextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // 5. Error banner
            uiState.errorMessage?.let { error ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22F4212E))
                            .border(1.dp, Color(0x66F4212E), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = XRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = error, color = TextPureWhite, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 6. Extracted Tweet Card (Author + Text + Multi-Video Selector + Preview Player + Metrics)
            uiState.currentTweet?.let { tweet ->
                val activeVideo = tweet.videos.getOrNull(uiState.selectedMediaIndex)
                    ?: tweet.videos.firstOrNull()
                val previewUrl = uiState.selectedQuality?.url ?: activeVideo?.variants?.firstOrNull()?.url ?: ""

                item {
                    XPostCard(
                        tweet = tweet,
                        selectedMediaIndex = uiState.selectedMediaIndex,
                        onSelectMediaIndex = { idx ->
                            viewModel.selectMediaIndex(idx)
                        },
                        isPlaying = uiState.isVideoPlaying,
                        activePlayingUrl = uiState.activePlayingUrl,
                        previewUrl = previewUrl,
                        onPlayClick = { viewModel.playVideo(previewUrl) },
                        onCloseClick = { viewModel.stopVideo() }
                    )
                }

                // 7. Available Stream Qualities (1080p, 720p, 480p, 4K) using QualityButton
                activeVideo?.let { video ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Available Qualities",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPureWhite,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                    if (tweet.videos.size > 1) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(Video ${uiState.selectedMediaIndex + 1})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = XBlue
                                        )
                                    }
                                }
                                Text(
                                    text = "Tap to unlock & download",
                                    fontSize = 12.sp,
                                    color = XTextSecondary
                                )
                            }

                            // Render QualityButton for every variant (HD/1080p, 720p, 480p, 4K)
                            video.variants.forEach { variant ->
                                val isSelected = uiState.selectedQuality?.url == variant.url && uiState.selectedQuality?.qualityLabel == variant.qualityLabel

                                QualityButton(
                                    variant = variant,
                                    isSelected = isSelected,
                                    is1080pUnlocked = is1080pUnlocked,
                                    is4KUnlocked = is4KUnlocked,
                                    isVipActive = uiState.isVipActive,
                                    daily720pCount = daily720pCount,
                                    adsWatchedFor4K = adsWatchedFor4K,
                                    onClick = {
                                        viewModel.selectQuality(variant)
                                        viewModel.startDownload(variant)
                                    }
                                )
                            }
                        }
                    }
                }

                // 8. Download Progress / Vault Actions
                item {
                    val currentQuality = uiState.selectedQuality ?: activeVideo?.variants?.firstOrNull()

                    Surface(
                        color = XDarkSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, XCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            when (val downloadState = uiState.downloadState) {
                                is DownloadState.Progress -> {
                                    Text(
                                        text = "Downloading stream (${currentQuality?.qualityLabel ?: "Video"})...",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPureWhite,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadState.progressPercent / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = XBlue,
                                        trackColor = XDarkSurfaceElevated
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${downloadState.progressPercent}%",
                                            color = XBlue,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                        TextButton(onClick = { viewModel.cancelDownload() }) {
                                            Text("Cancel", color = XTextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                is DownloadState.Success -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = XGreen)
                                        Text(
                                            text = "Saved to Downloads Vault!",
                                            fontWeight = FontWeight.Bold,
                                            color = XGreen,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = onNavigateToLibrary,
                                            colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = XBlack, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Open Vault", color = XBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                try {
                                                    val file = File(downloadState.downloadedVideo.localFilePath)
                                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, "video/mp4")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Cannot open: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, XCardBorder),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Play Now", color = XTextPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }

                                else -> {
                                    // Primary Download Action Button (X.com white pill button)
                                    Button(
                                        onClick = { viewModel.startDownload() },
                                        colors = ButtonDefaults.buttonColors(containerColor = TextPureWhite),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .testTag("start_download_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            tint = XBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Download ${currentQuality?.qualityLabel ?: "Video"}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp,
                                            color = XBlack
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 9. Permanent Footer Cross Promo Section
            item {
                MoreAppsFooterSection()
            }
        }
    }
}
