package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdsConfig
import com.example.ads.AdsManager
import com.example.data.DownloadedVideo
import com.example.data.VideoRepository
import com.example.downloader.DownloadState
import com.example.downloader.VideoDownloader
import com.example.model.QualityTier
import com.example.model.TweetData
import com.example.model.VideoVariant
import com.example.network.TwitterExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val selectedTab: Int = 0, // 0 = Downloader, 1 = Media Vault, 2 = VIP & Settings
    val urlInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentTweet: TweetData? = null,
    val selectedMediaIndex: Int = 0,
    val selectedQuality: VideoVariant? = null,
    val downloadState: DownloadState = DownloadState.Idle,
    val statusMessage: String? = null,
    // Preview player state
    val isVideoPlaying: Boolean = false,
    val activePlayingUrl: String? = null,
    // VIP state
    val isVipActive: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VideoRepository = VideoRepository(application)
    private val prefs = application.getSharedPreferences("allvid_user_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val downloadedVideos: StateFlow<List<DownloadedVideo>>

    // Ads states from AdsManager (ads.ts)
    val downloadCount: StateFlow<Int> = AdsManager.downloadCount
    val daily720pDownloads: StateFlow<Int> = AdsManager.daily720pDownloads
    val is1080pUnlocked: StateFlow<Boolean> = AdsManager.is1080pUnlocked
    val is4KUnlocked: StateFlow<Boolean> = AdsManager.is4KUnlocked
    val adsWatchedFor4K: StateFlow<Int> = AdsManager.adsWatchedFor4K
    val isWebMode: StateFlow<Boolean> = AdsManager.isWebMode
    val showInterstitial: StateFlow<Boolean> = AdsManager.showInterstitial
    val showMoreApps: StateFlow<Boolean> = AdsManager.showMoreApps
    val activeRewardedTarget: StateFlow<QualityTier?> = AdsManager.activeRewardedTarget

    init {
        // initAds()
        AdsManager.initAds(application)

        val savedVip = prefs.getBoolean("is_vip_active", false)
        if (savedVip) {
            AdsManager.unlockAllDirect()
        }

        _uiState.update { it.copy(isVipActive = savedVip) }

        downloadedVideos = repository.allVideos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Preload demo SpaceX post
        loadSampleTweet("sample_spacex")
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setWebMode(isWeb: Boolean) {
        AdsManager.setWebMode(isWeb)
    }

    fun setVipActive(active: Boolean) {
        prefs.edit().putBoolean("is_vip_active", active).apply()
        _uiState.update { it.copy(isVipActive = active) }
        if (active) {
            AdsManager.unlockAllDirect()
            _uiState.update { it.copy(statusMessage = "VIP $29/mo Activated! All 1080p & 4K unlocked, zero ads.") }
        } else {
            AdsManager.resetAll()
            _uiState.update { it.copy(statusMessage = "VIP deactivated. Standard quality limits applied.") }
        }
    }

    fun onUrlChanged(newUrl: String) {
        _uiState.update { it.copy(urlInput = newUrl, errorMessage = null) }
    }

    fun clearUrl() {
        _uiState.update { it.copy(urlInput = "", errorMessage = null) }
    }

    fun extractTweet(customUrl: String? = null) {
        val targetUrl = customUrl ?: _uiState.value.urlInput.trim()
        if (targetUrl.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter or paste any video URL") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    downloadState = DownloadState.Idle,
                    isVideoPlaying = false
                )
            }

            val result = TwitterExtractor.extract(targetUrl)
            result.fold(
                onSuccess = { tweet ->
                    val firstVideo = tweet.videos.firstOrNull()
                    val defaultVariant = firstVideo?.variants?.firstOrNull { it.isFree }
                        ?: firstVideo?.variants?.firstOrNull()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentTweet = tweet,
                            selectedMediaIndex = 0,
                            selectedQuality = defaultVariant,
                            urlInput = if (customUrl != null) customUrl else it.urlInput,
                            statusMessage = "Extracted ${tweet.videos.size} media stream(s)"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to extract video from URL"
                        )
                    }
                }
            )
        }
    }

    fun loadSampleTweet(sampleKey: String) {
        val sample = TwitterExtractor.getCuratedSampleTweet(sampleKey)
        if (sample != null) {
            val firstVideo = sample.videos.firstOrNull()
            val defaultVariant = firstVideo?.variants?.firstOrNull { it.isFree }
                ?: firstVideo?.variants?.firstOrNull()

            _uiState.update {
                it.copy(
                    urlInput = sample.originalUrl,
                    currentTweet = sample,
                    selectedMediaIndex = 0,
                    selectedQuality = defaultVariant,
                    errorMessage = null,
                    downloadState = DownloadState.Idle,
                    statusMessage = "Loaded preset stream from @${sample.authorUsername}"
                )
            }
        }
    }

    fun selectMediaIndex(index: Int) {
        val currentTweet = _uiState.value.currentTweet ?: return
        if (index in currentTweet.videos.indices) {
            val video = currentTweet.videos[index]
            val defaultVariant = video.variants.find { it.isFree } ?: video.variants.firstOrNull()
            _uiState.update {
                it.copy(
                    selectedMediaIndex = index,
                    selectedQuality = defaultVariant,
                    activePlayingUrl = null
                )
            }
        }
    }

    fun selectQuality(variant: VideoVariant) {
        _uiState.update { it.copy(selectedQuality = variant) }
    }

    /**
     * Download execution using unlockQuality() from ads.ts
     */
    fun startDownload(targetVariant: VideoVariant? = null) {
        val tweet = _uiState.value.currentTweet ?: return
        val variant = targetVariant ?: _uiState.value.selectedQuality ?: return

        _uiState.update { it.copy(selectedQuality = variant) }

        // unlockQuality() from ads.ts handles:
        // - 720p: 3x/day free limit check
        // - 1080p: 1 Rewarded Ad (24h expiry)
        // - 1440p / 4K: Interstitial + Rewarded (2 Ads, 24h expiry)
        AdsManager.unlockQuality(variant.tier, isVip = _uiState.value.isVipActive) { unlocked ->
            if (unlocked) {
                // If this is not an ad-gated unlock or ad is completed, check for interstitial interval (every 2 downloads)
                val isAdOrModalShown = AdsManager.onDownloadTriggered {
                    executeDownload(tweet, variant)
                }
                if (!isAdOrModalShown) {
                    executeDownload(tweet, variant)
                }
            }
        }
    }

    private fun executeDownload(tweet: TweetData, variant: VideoVariant) {
        AdsManager.recordDownloadStarted(variant.tier)

        viewModelScope.launch {
            VideoDownloader.downloadVideo(
                context = getApplication(),
                tweet = tweet,
                variant = variant
            ).collect { state ->
                _uiState.update { it.copy(downloadState = state) }
                if (state is DownloadState.Success) {
                    AdsManager.onDownloadCompleted()
                }
            }
        }
    }

    fun cancelDownload() {
        _uiState.update { it.copy(downloadState = DownloadState.Idle) }
    }

    fun playVideo(url: String) {
        _uiState.update {
            it.copy(
                isVideoPlaying = true,
                activePlayingUrl = url
            )
        }
    }

    fun stopVideo() {
        _uiState.update {
            it.copy(
                isVideoPlaying = false,
                activePlayingUrl = null
            )
        }
    }

    fun deleteDownloadedVideo(video: DownloadedVideo) {
        viewModelScope.launch {
            repository.delete(video)
            _uiState.update { it.copy(statusMessage = "Deleted video from Media Vault") }
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.deleteAll()
            _uiState.update { it.copy(statusMessage = "Cleared all media from vault") }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // Ads delegate actions
    fun onRewardedAdCompleted() {
        AdsManager.onRewardedAdCompleted()
    }

    fun dismissRewardedAd() {
        AdsManager.dismissRewarded()
    }

    fun dismissInterstitialAd() {
        AdsManager.dismissInterstitial()
    }

    fun triggerMoreAppsModal() {
        AdsManager.triggerMoreAppsModal()
    }

    fun dismissMoreAppsModal(dontShowToday: Boolean = false) {
        AdsManager.dismissMoreApps(dontShowToday)
    }

    fun resetAdsState() {
        AdsManager.resetAll()
        _uiState.update { it.copy(statusMessage = "All ads state, counters, and unlocks reset.") }
    }
}
