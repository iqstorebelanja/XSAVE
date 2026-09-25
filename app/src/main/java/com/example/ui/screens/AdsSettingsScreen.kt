package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ads.AdsConfig
import com.example.ads.AdsManager
import com.example.model.QualityTier
import com.example.ui.MainViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.LinearGridBackground
import com.example.ui.theme.*

@Composable
fun AdsSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val downloadCount by viewModel.downloadCount.collectAsState()
    val daily720pDownloads by viewModel.daily720pDownloads.collectAsState()
    val is1080pUnlocked by viewModel.is1080pUnlocked.collectAsState()
    val is4KUnlocked by viewModel.is4KUnlocked.collectAsState()
    val adsWatchedFor4K by viewModel.adsWatchedFor4K.collectAsState()
    val isWebMode by viewModel.isWebMode.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LinearGridBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "VIP & Monetization Hub",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPureWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Quality gate, $29/mo VIP tier, and ads.ts AdMob telemetry",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Web vs APK Mode Selector
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Platform Mode: ${if (isWebMode) "Web Version" else "APK Version"}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPureWhite
                                )
                                Text(
                                    text = if (isWebMode) "AdSense 5s countdown modal (Simulated Rewarded)" else "Real AdMob Rewarded ID (ca-app-pub-.../5224354917)",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            Switch(
                                checked = isWebMode,
                                onCheckedChange = { viewModel.setWebMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureBlack,
                                    checkedTrackColor = NeonBlue,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Color(0x22FFFFFF)
                                )
                            )
                        }
                    }
                }
            }

            // VIP Membership Card ($29/mo Linear Style)
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "VIP Membership",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPureWhite
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonBlue)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "\$29/mo",
                                            color = PureBlack,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Text(
                                    text = if (uiState.isVipActive) "All 1080p & 4K streams unlocked • Zero Ads" else "Toggle ON to simulate active VIP subscription",
                                    fontSize = 11.sp,
                                    color = if (uiState.isVipActive) AccentGreen else TextSecondary
                                )
                            }

                            Switch(
                                checked = uiState.isVipActive,
                                onCheckedChange = { viewModel.setVipActive(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureBlack,
                                    checkedTrackColor = NeonBlue,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Color(0x22FFFFFF)
                                )
                            )
                        }
                    }
                }
            }

            // Quality Gate Status Dashboard
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Quality Gate Status (ads.ts)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPureWhite
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 720p FREE with 3x/day limit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("720p (HD) Quality", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Free limit: $daily720pDownloads / ${AdsConfig.FREE_720P_DAILY_LIMIT} used today", fontSize = 11.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (daily720pDownloads < AdsConfig.FREE_720P_DAILY_LIMIT) Color(0x222EE59D) else Color(0x22FF3366))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (daily720pDownloads < AdsConfig.FREE_720P_DAILY_LIMIT) "FREE ($daily720pDownloads/3)" else "LIMIT REACHED (1 Ad)",
                                    color = if (daily720pDownloads < AdsConfig.FREE_720P_DAILY_LIMIT) AccentGreen else XRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 10.dp))

                        // 1080p Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("1080p (Full HD) Quality", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Requires 1 Rewarded Ad (24h expiry)", fontSize = 11.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (is1080pUnlocked || uiState.isVipActive) Color(0x222EE59D) else Color(0x18FFFFFF))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (is1080pUnlocked || uiState.isVipActive) "UNLOCKED (24H)" else "LOCKED (1 Rewarded)",
                                    color = if (is1080pUnlocked || uiState.isVipActive) AccentGreen else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 10.dp))

                        // 4K Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("1440p / 4K (Original) Quality", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("Requires Interstitial + Rewarded ($adsWatchedFor4K/2 completed)", fontSize = 11.sp, color = TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (is4KUnlocked || uiState.isVipActive) Color(0x2200D1FF) else Color(0x18FFFFFF))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (is4KUnlocked || uiState.isVipActive) "UNLOCKED (24H)" else "LOCKED ($adsWatchedFor4K/2 Ads)",
                                    color = if (is4KUnlocked || uiState.isVipActive) NeonBlue else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // AdMob Configuration & Interstitial Rule
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "AdMob Telemetry (Real IDs)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPureWhite
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("App ID: ${AdsConfig.APP_ID}", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rewarded Unit ID: ${AdsConfig.REWARDED_AD_UNIT_ID}", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Interstitial Unit ID: ${AdsConfig.INTERSTITIAL_AD_UNIT_ID}", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Banner Unit ID: ${AdsConfig.BANNER_AD_UNIT_ID}", fontSize = 11.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Downloads Count:", fontSize = 12.sp, color = TextSecondary)
                            Text("$downloadCount downloads", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonBlue)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerMoreAppsModal() },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassSurfaceHover),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Test MoreApps", color = TextPureWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.resetAdsState()
                                    Toast.makeText(context, "Locks and daily counters reset", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset All", color = XRed, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
