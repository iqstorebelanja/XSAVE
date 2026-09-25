package com.example.ads

import android.content.Context
import android.content.SharedPreferences
import com.example.model.QualityTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AdsManager / ads.ts logic implementation:
 * - 720p: FREE with 3x/day daily limit
 * - 1080p: LOCKED (1 Rewarded Ad) with 24h localStorage / SharedPreferences expiry
 * - 1440p / 4K: LOCKED (Interstitial + Rewarded, 2 Ads) with 24h expiry
 * - initAds() method
 * - unlockQuality() core handler
 * - Web vs APK mode emulation support:
 *     - Web Mode: 5s countdown modal with AdSense placeholder div
 *     - APK Mode: Real AdMob Rewarded ID (ca-app-pub-3940256099942544/5224354917)
 */
object AdsManager {

    // AdMob IDs from ads.ts
    const val APP_ID = AdsConfig.APP_ID
    const val BANNER_ID = AdsConfig.BANNER_AD_UNIT_ID
    const val INTERSTITIAL_ID = AdsConfig.INTERSTITIAL_AD_UNIT_ID
    const val REWARDED_ID = AdsConfig.REWARDED_AD_UNIT_ID
    const val INTERSTITIAL_FREQUENCY_DOWNLOADS = AdsConfig.INTERSTITIAL_FREQUENCY_DOWNLOADS
    const val FREE_720P_DAILY_LIMIT = AdsConfig.FREE_720P_DAILY_LIMIT

    private const val PREFS_NAME = "xsave_ads_prefs"
    private const val KEY_DOWNLOAD_COUNT = "download_count"
    private const val KEY_1080P_UNLOCKED_TIMESTAMP = "timestamp_1080p_unlocked"
    private const val KEY_4K_UNLOCKED_TIMESTAMP = "timestamp_4k_unlocked"
    private const val KEY_4K_ADS_WATCHED = "ads_watched_for_4k"
    private const val KEY_DAILY_720P_DATE = "daily_720p_date"
    private const val KEY_DAILY_720P_COUNT = "daily_720p_count"
    private const val KEY_MORE_APPS_DISMISSED_DATE = "more_apps_dismissed_date"
    private const val KEY_IS_WEB_MODE = "is_web_mode"

    private const val TWENTY_FOUR_HOURS_MS = 24L * 60L * 60L * 1000L

    private var prefs: SharedPreferences? = null

    // Download counter
    private val _downloadCount = MutableStateFlow(0)
    val downloadCount: StateFlow<Int> = _downloadCount.asStateFlow()

    // 720p Daily Free usage count (Max 3x / day)
    private val _daily720pDownloads = MutableStateFlow(0)
    val daily720pDownloads: StateFlow<Int> = _daily720pDownloads.asStateFlow()

    // 1080p Unlock state (valid for 24h)
    private val _is1080pUnlocked = MutableStateFlow(false)
    val is1080pUnlocked: StateFlow<Boolean> = _is1080pUnlocked.asStateFlow()

    // 4K (1440p / 4K) Unlock state (valid for 24h, requires Interstitial + Rewarded / 2 ads)
    private val _is4KUnlocked = MutableStateFlow(false)
    val is4KUnlocked: StateFlow<Boolean> = _is4KUnlocked.asStateFlow()

    private val _adsWatchedFor4K = MutableStateFlow(0)
    val adsWatchedFor4K: StateFlow<Int> = _adsWatchedFor4K.asStateFlow()

    // Mode: Web version (simulate 5s countdown modal with AdSense div) vs APK version (Real AdMob ID)
    private val _isWebMode = MutableStateFlow(false)
    val isWebMode: StateFlow<Boolean> = _isWebMode.asStateFlow()

    // Interstitial & Rewarded Dialog Triggers
    private val _showInterstitial = MutableStateFlow(false)
    val showInterstitial: StateFlow<Boolean> = _showInterstitial.asStateFlow()

    private val _activeRewardedTarget = MutableStateFlow<QualityTier?>(null)
    val activeRewardedTarget: StateFlow<QualityTier?> = _activeRewardedTarget.asStateFlow()

    // MoreApps Modal Trigger
    private val _showMoreApps = MutableStateFlow(false)
    val showMoreApps: StateFlow<Boolean> = _showMoreApps.asStateFlow()

    // Pending callbacks
    private var pendingQualityUnlockCallback: ((Boolean) -> Unit)? = null
    private var pendingInterstitialCallback: (() -> Unit)? = null

    /**
     * initAds() - initializes Ads state, checks 24h expiry and daily 720p limit
     */
    fun initAds(context: Context) {
        initialize(context)
    }

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Download counter
        _downloadCount.value = prefs?.getInt(KEY_DOWNLOAD_COUNT, 0) ?: 0
        _isWebMode.value = prefs?.getBoolean(KEY_IS_WEB_MODE, false) ?: false

        checkAndRefreshExpiry()
    }

    fun setWebMode(webMode: Boolean) {
        _isWebMode.value = webMode
        prefs?.edit()?.putBoolean(KEY_IS_WEB_MODE, webMode)?.apply()
    }

    /**
     * Checks 24h expiry on 1080p and 4K unlocks, and resets daily 720p quota if new day
     */
    fun checkAndRefreshExpiry() {
        val now = System.currentTimeMillis()
        val today = getTodayDateString()

        // 1. Check 720p daily quota
        val saved720pDate = prefs?.getString(KEY_DAILY_720P_DATE, null)
        if (saved720pDate != today) {
            prefs?.edit()
                ?.putString(KEY_DAILY_720P_DATE, today)
                ?.putInt(KEY_DAILY_720P_COUNT, 0)
                ?.apply()
            _daily720pDownloads.value = 0
        } else {
            _daily720pDownloads.value = prefs?.getInt(KEY_DAILY_720P_COUNT, 0) ?: 0
        }

        // 2. Check 1080p 24h expiry
        val timestamp1080p = prefs?.getLong(KEY_1080P_UNLOCKED_TIMESTAMP, 0L) ?: 0L
        val is1080pValid = (now - timestamp1080p) < TWENTY_FOUR_HOURS_MS && timestamp1080p > 0L
        _is1080pUnlocked.value = is1080pValid
        if (!is1080pValid && timestamp1080p > 0L) {
            prefs?.edit()?.remove(KEY_1080P_UNLOCKED_TIMESTAMP)?.apply()
        }

        // 3. Check 4K 24h expiry
        val timestamp4K = prefs?.getLong(KEY_4K_UNLOCKED_TIMESTAMP, 0L) ?: 0L
        val is4KValid = (now - timestamp4K) < TWENTY_FOUR_HOURS_MS && timestamp4K > 0L
        _is4KUnlocked.value = is4KValid
        val saved4KAds = prefs?.getInt(KEY_4K_ADS_WATCHED, 0) ?: 0
        _adsWatchedFor4K.value = if (is4KValid) 2 else saved4KAds
        if (!is4KValid && timestamp4K > 0L) {
            prefs?.edit()?.remove(KEY_4K_UNLOCKED_TIMESTAMP)?.putInt(KEY_4K_ADS_WATCHED, 0)?.apply()
            _adsWatchedFor4K.value = 0
        }
    }

    /**
     * Remaining seconds or hours for 24h unlock
     */
    fun getUnlockRemainingMillis(tier: QualityTier): Long {
        val now = System.currentTimeMillis()
        val timestamp = when (tier) {
            QualityTier.LOCKED_1_AD -> prefs?.getLong(KEY_1080P_UNLOCKED_TIMESTAMP, 0L) ?: 0L
            QualityTier.LOCKED_2_ADS -> prefs?.getLong(KEY_4K_UNLOCKED_TIMESTAMP, 0L) ?: 0L
            else -> return 0L
        }
        if (timestamp <= 0L) return 0L
        val remaining = TWENTY_FOUR_HOURS_MS - (now - timestamp)
        return if (remaining > 0L) remaining else 0L
    }

    /**
     * Checks if a tier is currently unlocked (honoring 24h expiry and VIP)
     */
    fun isTierUnlocked(tier: QualityTier, isVip: Boolean = false): Boolean {
        if (isVip) return true
        checkAndRefreshExpiry()
        return when (tier) {
            QualityTier.FREE -> {
                // Free 720p has 3x/day limit
                _daily720pDownloads.value < FREE_720P_DAILY_LIMIT
            }
            QualityTier.LOCKED_1_AD -> _is1080pUnlocked.value
            QualityTier.LOCKED_2_ADS -> _is4KUnlocked.value
        }
    }

    /**
     * unlockQuality() - Core function matching ads.ts
     * Replaces old unlock logic:
     * - 720p: If within 3x limit, immediately proceed. If daily limit reached, prompt rewarded ad to continue.
     * - 1080p: Requires 1 Rewarded Ad (granted for 24h)
     * - 1440p / 4K: Requires Interstitial + Rewarded (2 Ads total, granted for 24h)
     */
    fun unlockQuality(tier: QualityTier, isVip: Boolean = false, onUnlocked: (Boolean) -> Unit) {
        if (isVip) {
            onUnlocked(true)
            return
        }

        checkAndRefreshExpiry()

        // 720p free tier
        if (tier == QualityTier.FREE) {
            if (_daily720pDownloads.value < FREE_720P_DAILY_LIMIT) {
                // Allowed free
                onUnlocked(true)
            } else {
                // Daily limit 3x exceeded -> show rewarded ad to unlock extra 720p download
                pendingQualityUnlockCallback = onUnlocked
                _activeRewardedTarget.value = QualityTier.FREE
            }
            return
        }

        // 1080p
        if (tier == QualityTier.LOCKED_1_AD) {
            if (_is1080pUnlocked.value) {
                onUnlocked(true)
            } else {
                pendingQualityUnlockCallback = onUnlocked
                _activeRewardedTarget.value = QualityTier.LOCKED_1_AD
            }
            return
        }

        // 4K (1440p / 4K Original) - requires 2 ads (Interstitial + Rewarded)
        if (tier == QualityTier.LOCKED_2_ADS) {
            if (_is4KUnlocked.value) {
                onUnlocked(true)
            } else {
                pendingQualityUnlockCallback = onUnlocked
                _activeRewardedTarget.value = QualityTier.LOCKED_2_ADS
            }
            return
        }

        onUnlocked(true)
    }

    /**
     * Called when a rewarded video / countdown is completed:
     * - For 1080p: unlock for 24h
     * - For 4K: step 1 (interstitial / ad 1), then step 2 (rewarded ad 2) -> unlock for 24h
     * - For 720p limit exceeded: unlock 1 extra download
     */
    fun onRewardedAdCompleted() {
        val target = _activeRewardedTarget.value ?: return
        val now = System.currentTimeMillis()

        when (target) {
            QualityTier.FREE -> {
                // Bonus free download granted
                _activeRewardedTarget.value = null
                val cb = pendingQualityUnlockCallback
                pendingQualityUnlockCallback = null
                cb?.invoke(true)
            }
            QualityTier.LOCKED_1_AD -> {
                // 1080p unlocked for 24 hours
                _is1080pUnlocked.value = true
                prefs?.edit()?.putLong(KEY_1080P_UNLOCKED_TIMESTAMP, now)?.apply()
                _activeRewardedTarget.value = null
                val cb = pendingQualityUnlockCallback
                pendingQualityUnlockCallback = null
                cb?.invoke(true)
            }
            QualityTier.LOCKED_2_ADS -> {
                // 4K requires 2 ads (Interstitial + Rewarded)
                val current = _adsWatchedFor4K.value + 1
                _adsWatchedFor4K.value = current
                prefs?.edit()?.putInt(KEY_4K_ADS_WATCHED, current)?.apply()

                if (current >= 2) {
                    _is4KUnlocked.value = true
                    prefs?.edit()?.putLong(KEY_4K_UNLOCKED_TIMESTAMP, now)?.apply()
                    _activeRewardedTarget.value = null
                    val cb = pendingQualityUnlockCallback
                    pendingQualityUnlockCallback = null
                    cb?.invoke(true)
                }
            }
        }
    }

    /**
     * Increments download counter and tracks daily 720p usage
     */
    fun recordDownloadStarted(tier: QualityTier) {
        val newCount = _downloadCount.value + 1
        _downloadCount.value = newCount
        prefs?.edit()?.putInt(KEY_DOWNLOAD_COUNT, newCount)?.apply()

        if (tier == QualityTier.FREE) {
            val dailyCount = _daily720pDownloads.value + 1
            _daily720pDownloads.value = dailyCount
            val today = getTodayDateString()
            prefs?.edit()
                ?.putString(KEY_DAILY_720P_DATE, today)
                ?.putInt(KEY_DAILY_720P_COUNT, dailyCount)
                ?.apply()
        }
    }

    /**
     * Check if interstitial or MoreApps should show before download
     */
    fun onDownloadTriggered(onAdClosed: () -> Unit): Boolean {
        val newCount = _downloadCount.value + 1

        // Cross Promo Rule:
        if (newCount == 3 && !isMoreAppsDismissedToday()) {
            pendingInterstitialCallback = onAdClosed
            _showMoreApps.value = true
            return true
        }

        // Standard Interstitial Rule: Every 2 downloads
        if (newCount % INTERSTITIAL_FREQUENCY_DOWNLOADS == 0) {
            pendingInterstitialCallback = onAdClosed
            _showInterstitial.value = true
            return true
        } else {
            onAdClosed()
            return false
        }
    }

    fun onDownloadCompleted() {
        if (_downloadCount.value == 2 && !isMoreAppsDismissedToday()) {
            _showMoreApps.value = true
        }
    }

    fun dismissInterstitial() {
        _showInterstitial.value = false
        val cb = pendingInterstitialCallback
        pendingInterstitialCallback = null
        cb?.invoke()
    }

    fun dismissRewarded() {
        _activeRewardedTarget.value = null
        val cb = pendingQualityUnlockCallback
        pendingQualityUnlockCallback = null
        cb?.invoke(false)
    }

    fun triggerMoreAppsModal() {
        _showMoreApps.value = true
    }

    fun dismissMoreApps(dontShowToday: Boolean = false) {
        _showMoreApps.value = false
        if (dontShowToday) {
            setMoreAppsDismissedToday()
        }
        val cb = pendingInterstitialCallback
        if (cb != null && _downloadCount.value == 3) {
            _showInterstitial.value = true
        }
    }

    fun isMoreAppsDismissedToday(): Boolean {
        val today = getTodayDateString()
        val savedDate = prefs?.getString(KEY_MORE_APPS_DISMISSED_DATE, null)
        return today == savedDate
    }

    fun setMoreAppsDismissedToday() {
        val today = getTodayDateString()
        prefs?.edit()?.putString(KEY_MORE_APPS_DISMISSED_DATE, today)?.apply()
    }

    fun resetAll() {
        val editor = prefs?.edit() ?: return
        editor.remove(KEY_DOWNLOAD_COUNT)
        editor.remove(KEY_1080P_UNLOCKED_TIMESTAMP)
        editor.remove(KEY_4K_UNLOCKED_TIMESTAMP)
        editor.remove(KEY_4K_ADS_WATCHED)
        editor.remove(KEY_DAILY_720P_COUNT)
        editor.remove(KEY_MORE_APPS_DISMISSED_DATE)
        editor.apply()

        _downloadCount.value = 0
        _is1080pUnlocked.value = false
        _is4KUnlocked.value = false
        _adsWatchedFor4K.value = 0
        _daily720pDownloads.value = 0
    }

    fun unlockAllDirect() {
        val now = System.currentTimeMillis()
        prefs?.edit()
            ?.putLong(KEY_1080P_UNLOCKED_TIMESTAMP, now)
            ?.putLong(KEY_4K_UNLOCKED_TIMESTAMP, now)
            ?.putInt(KEY_4K_ADS_WATCHED, 2)
            ?.apply()

        _is1080pUnlocked.value = true
        _is4KUnlocked.value = true
        _adsWatchedFor4K.value = 2
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
