package com.example.model

/**
 * Quality Tier definitions with AdMob gating from ads.ts:
 * - 720p / 480p = FREE (3x/day free limit for 720p)
 * - 1080p = LOCKED 1 Rewarded Ad
 * - 1440p / 4K Original = LOCKED Interstitial + Rewarded (2 Ads)
 */
enum class QualityTier(val code: String, val requiredAds: Int, val badgeText: String) {
    FREE("720p", 0, "FREE"),
    LOCKED_1_AD("1080p", 1, "1 REWARDED"),
    LOCKED_2_ADS("4K", 2, "INTERSTITIAL + REWARDED");

    companion object {
        fun fromLabel(label: String): QualityTier {
            val lower = label.lowercase()
            return when {
                lower.contains("4k") || lower.contains("1440p") || lower.contains("2160p") -> LOCKED_2_ADS
                lower.contains("1080p") || lower.contains("1080") -> LOCKED_1_AD
                else -> FREE
            }
        }
    }
}

data class VideoVariant(
    val qualityLabel: String,       // e.g. "1440p / 4K (Original)", "1080p (Full HD)", "720p (HD)", "480p (SD)"
    val resolution: String,         // e.g. "3840x2160", "1080x1920", "720x1280", "480x852"
    val bitrate: Long = 0L,         // bits per second
    val url: String,                // Direct MP4 link
    val tier: QualityTier = QualityTier.FREE,
    val estimatedSizeBytes: Long = 0L
) {
    val isFree: Boolean
        get() = tier == QualityTier.FREE

    val isLocked1080p: Boolean
        get() = tier == QualityTier.LOCKED_1_AD

    val isLocked4K: Boolean
        get() = tier == QualityTier.LOCKED_2_ADS
}

data class VideoMedia(
    val mediaIndex: Int = 0,
    val type: String = "video",      // "video" or "animated_gif"
    val thumbnailUrl: String,
    val durationMs: Long = 0L,
    val variants: List<VideoVariant>
)

data class TweetData(
    val id: String,
    val originalUrl: String,
    val text: String,
    val authorName: String,
    val authorUsername: String,
    val authorAvatarUrl: String,
    val isVerified: Boolean = true,
    val createdAt: String = "",
    val likesCount: Int = 0,
    val retweetsCount: Int = 0,
    val repliesCount: Int = 0,
    val videos: List<VideoMedia>
)
