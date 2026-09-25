package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ads.AdsManager
import com.example.model.QualityTier
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * AdMob Banner Ad View (Persistent bottom ad)
 */
@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner_view"),
        color = PureBlack,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE5A93B))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "Ad",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                Column {
                    Text(
                        text = "Google AdMob • Adaptive Banner",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPureWhite
                    )
                    Text(
                        text = "ID: ${AdsManager.BANNER_ID.take(28)}...",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x1800D1FF))
                    .border(1.dp, Color(0x3300D1FF), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Sponsor",
                    fontSize = 10.sp,
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Interstitial Ad Dialog (Triggered every 2 downloads or step 1 of 4K)
 */
@Composable
fun InterstitialAdDialog(
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(3) }
    val canClose = countdown <= 0

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    Dialog(
        onDismissRequest = {
            if (canClose) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = canClose,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .testTag("admob_interstitial_dialog")
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE5A93B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AdMob Interstitial",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = "Every 2 Downloads",
                            fontSize = 11.sp,
                            color = NeonBlue
                        )
                    }

                    if (canClose) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassSurfaceElevated)
                                .testTag("interstitial_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Ad",
                                tint = TextPureWhite
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassSurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Skip in ${countdown}s",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Center Ad Content
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GlassSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NeonBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = PureBlack,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "AdMob Fullscreen Interstitial",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPureWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Unit ID: ${AdsManager.INTERSTITIAL_ID}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "XSave is 100% free! Every 2 downloads, an interstitial ad supports high-bandwidth video extraction.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (canClose) onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (canClose) "Continue to Download" else "Loading (${countdown}s)...",
                                fontWeight = FontWeight.Bold,
                                color = PureBlack
                            )
                        }
                    }
                }

                Text(
                    text = "Google AdMob Certified Test Ad",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Rewarded Ad Dialog:
 * Supports both:
 * 1. Web Version Mode: 5s countdown modal with AdSense placeholder div (simulate rewarded)
 * 2. APK Version Mode: Real AdMob Rewarded ID (ca-app-pub-3940256099942544/5224354917)
 */
@Composable
fun RewardedAdDialog(
    targetTier: QualityTier,
    currentWatchedCount: Int,
    isWebMode: Boolean = false,
    onAdCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(5) }
    var rewardEarned by remember { mutableStateOf(false) }

    val isFor4K = targetTier == QualityTier.LOCKED_2_ADS
    val isForDaily720p = targetTier == QualityTier.FREE
    val targetTitle = when {
        isFor4K -> "1440p / 4K Original (Valid 24h)"
        isForDaily720p -> "720p Extra Free Download"
        else -> "1080p (Full HD) (Valid 24h)"
    }
    val adsRequired = if (isFor4K) 2 else 1
    val currentStep = if (isFor4K) currentWatchedCount + 1 else 1

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
        rewardEarned = true
    }

    Dialog(
        onDismissRequest = {
            if (rewardEarned) {
                onAdCompleted()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = rewardEarned,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .testTag("admob_rewarded_dialog")
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isWebMode) NeonBlue else Color(0xFFE5A93B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isWebMode) "Google AdSense" else "AdMob Rewarded",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = if (isFor4K) "Unlock 4K (Ad $currentStep of $adsRequired)" else (if (isForDaily720p) "Daily Quota Bonus" else "Unlock 1080p 24h"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonBlue
                        )
                    }

                    if (rewardEarned) {
                        IconButton(
                            onClick = onAdCompleted,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextPureWhite
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassSurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                // Center Card: Displays AdSense placeholder div in Web mode or Real AdMob in APK mode
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GlassSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (rewardEarned) AccentGreen else GlassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Web Version: Simulated AdSense Placeholder Div
                        if (isWebMode) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PureBlack,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334285F4)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .testTag("adsense_placeholder_div")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "<!-- Google AdSense Responsive Ad Unit -->",
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = NeonBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "adsbygoogle = window.adsbygoogle || []).push({});",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Simulating rewarded ad countdown (5 seconds)...",
                                        fontSize = 11.sp,
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            // APK Version: Real AdMob ID indicator & graphic
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(if (rewardEarned) AccentGreen else NeonBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFor4K) Icons.Default.Star else Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = PureBlack,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (rewardEarned) {
                                if (isFor4K && currentStep < 2) "Ad 1 Finished! (Watch 2nd Ad for 4K)"
                                else "$targetTitle Unlocked!"
                            } else (if (isWebMode) "Watching Web Sponsored Video..." else "Watching AdMob Rewarded Video"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rewardEarned) AccentGreen else TextPureWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isWebMode) "Web Mode • AdSense Rewarded Simulation" else "AdMob Rewarded Unit: ${AdsManager.REWARDED_ID}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quality Gate Rule notice
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PureBlack,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Quality Gate Rule (ads.ts):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonBlue
                                )
                                Text(
                                    text = "• 720p: FREE (Daily limit 3x)\n• 1080p: LOCKED 1 Rewarded Ad (24h expiry)\n• 1440p/4K: LOCKED Interstitial + Rewarded (24h expiry)",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!rewardEarned) {
                            LinearProgressIndicator(
                                progress = { (5 - countdown) / 5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = NeonBlue,
                                trackColor = GlassSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Reward grants in ${countdown}s...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        } else {
                            Button(
                                onClick = onAdCompleted,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFor4K && currentStep < 2) NeonBlue else AccentGreen
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("claim_reward_button")
                            ) {
                                Text(
                                    text = if (isFor4K && currentStep < 2) "Watch Ad 2 to Complete 4K Unlock" else "Claim Reward & Download",
                                    color = PureBlack,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (isWebMode) "Google AdSense 5s Simulation Modal" else "Google AdMob Certified Rewarded Video Ad",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
