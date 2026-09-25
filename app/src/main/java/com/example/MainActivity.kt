package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.AdsManager
import com.example.ui.MainViewModel
import com.example.ui.components.AdMobBannerView
import com.example.ui.components.InterstitialAdDialog
import com.example.ui.components.MoreAppsDialog
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.XSaveTopBar
import com.example.ui.screens.AdsSettingsScreen
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle shared links from X or other video apps
        handleIncomingIntent(intent)

        setContent {
            XSaveTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val downloadedVideos by viewModel.downloadedVideos.collectAsStateWithLifecycle()
                val daily720pCount by viewModel.daily720pDownloads.collectAsStateWithLifecycle()
                val is1080pUnlocked by viewModel.is1080pUnlocked.collectAsStateWithLifecycle()
                val is4KUnlocked by viewModel.is4KUnlocked.collectAsStateWithLifecycle()
                val adsWatchedFor4K by viewModel.adsWatchedFor4K.collectAsStateWithLifecycle()
                val isWebMode by viewModel.isWebMode.collectAsStateWithLifecycle()
                val showInterstitial by viewModel.showInterstitial.collectAsStateWithLifecycle()
                val showMoreApps by viewModel.showMoreApps.collectAsStateWithLifecycle()
                val activeRewardedTarget by viewModel.activeRewardedTarget.collectAsStateWithLifecycle()

                // Init AdMob / ads.ts logic on start (equivalent to initAds() in useEffect)
                LaunchedEffect(Unit) {
                    AdsManager.initAds(applicationContext)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = PureBlack,
                    topBar = {
                        // Authentic X.com style Top Bar
                        XSaveTopBar(
                            isVipActive = uiState.isVipActive,
                            onVipToggle = { active ->
                                viewModel.setVipActive(active)
                            }
                        )
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PureBlack)
                                .border(1.dp, GlassBorder)
                        ) {
                            // AdMob Banner View (Hidden when VIP is active)
                            if (!uiState.isVipActive) {
                                AdMobBannerView()
                            }

                            // Bottom Navigation Tabs (Linear / Raycast Dark Glass style)
                            NavigationBar(
                                containerColor = PureBlack,
                                contentColor = TextPrimary,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(64.dp)
                            ) {
                                NavigationBarItem(
                                    selected = uiState.selectedTab == 0,
                                    onClick = { viewModel.selectTab(0) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Downloader"
                                        )
                                    },
                                    label = { Text("Downloader", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PureBlack,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_downloader")
                                )

                                NavigationBarItem(
                                    selected = uiState.selectedTab == 1,
                                    onClick = { viewModel.selectTab(1) },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (downloadedVideos.isNotEmpty()) {
                                                    Badge(
                                                        containerColor = NeonBlue,
                                                        contentColor = PureBlack
                                                    ) {
                                                        Text("${downloadedVideos.size}", fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VideoLibrary,
                                                contentDescription = "Media Vault"
                                            )
                                        }
                                    },
                                    label = { Text("Media Vault", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PureBlack,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_library")
                                )

                                NavigationBarItem(
                                    selected = uiState.selectedTab == 2,
                                    onClick = { viewModel.selectTab(2) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.MonetizationOn,
                                            contentDescription = "VIP & Ads"
                                        )
                                    },
                                    label = { Text("VIP & Ads", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PureBlack,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonBlue,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_tab_ads")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (uiState.selectedTab) {
                            0 -> DownloaderScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                is1080pUnlocked = is1080pUnlocked,
                                is4KUnlocked = is4KUnlocked,
                                daily720pCount = daily720pCount,
                                adsWatchedFor4K = adsWatchedFor4K,
                                onNavigateToLibrary = { viewModel.selectTab(1) }
                            )
                            1 -> LibraryScreen(
                                viewModel = viewModel,
                                videos = downloadedVideos,
                                onNavigateToDownloader = { viewModel.selectTab(0) }
                            )
                            2 -> AdsSettingsScreen(
                                viewModel = viewModel
                            )
                        }

                        // MoreApps Cross-Promo Dialog (Appears after 2nd download or before interstitial on 3rd download)
                        if (showMoreApps && !uiState.isVipActive) {
                            MoreAppsDialog(
                                onDismiss = { dontShowToday ->
                                    viewModel.dismissMoreAppsModal(dontShowToday)
                                }
                            )
                        }

                        // Interstitial Ad Overlay (Skipped if VIP is active)
                        if (showInterstitial && !uiState.isVipActive) {
                            InterstitialAdDialog(
                                onDismiss = { viewModel.dismissInterstitialAd() }
                            )
                        }

                        // Rewarded Ad Overlay (Web 5s countdown modal or Real AdMob APK)
                        if (!uiState.isVipActive) {
                            activeRewardedTarget?.let { target ->
                                RewardedAdDialog(
                                    targetTier = target,
                                    currentWatchedCount = adsWatchedFor4K,
                                    isWebMode = isWebMode,
                                    onAdCompleted = { viewModel.onRewardedAdCompleted() },
                                    onDismiss = { viewModel.dismissRewardedAd() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                viewModel.onUrlChanged(sharedText)
                viewModel.extractTweet(sharedText)
                viewModel.selectTab(0)
            }
        }
    }
}
