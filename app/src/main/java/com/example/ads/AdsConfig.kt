package com.example.ads

import com.example.model.QualityTier

/**
 * Port of ads.ts configuration & quality rules for XSave / ALLVID
 * Matches src/utils/ads.ts uploaded by user:
 * - 720p (HD) = FREE (with daily limit 3x/day check)
 * - 1080p (Full HD) = LOCKED (1 Rewarded Ad)
 * - 1440p / 4K (Original) = LOCKED (Interstitial + Rewarded or 2 Ads)
 */

object AdsConfig {
    const val APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    // Real / standard test AdMob rewarded ID from ads.ts
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val INTERSTITIAL_FREQUENCY_DOWNLOADS = 2
    const val FREE_720P_DAILY_LIMIT = 3
    const val UNLOCK_EXPIRY_HOURS = 24L
}

data class QualityGateRule(
    val tier: QualityTier,
    val label: String,
    val resolution: String,
    val isFree: Boolean,
    val requiredAdsCount: Int,
    val lockReasonIndonesian: String
)

val QUALITY_GATE_RULES: Map<String, QualityGateRule> = mapOf(
    "480p" to QualityGateRule(
        tier = QualityTier.FREE,
        label = "480p (SD)",
        resolution = "480x854",
        isFree = true,
        requiredAdsCount = 0,
        lockReasonIndonesian = "GRATIS"
    ),
    "720p" to QualityGateRule(
        tier = QualityTier.FREE,
        label = "720p (HD)",
        resolution = "720x1280",
        isFree = true,
        requiredAdsCount = 0,
        lockReasonIndonesian = "GRATIS (Limit 3x/hari)"
    ),
    "1080p" to QualityGateRule(
        tier = QualityTier.LOCKED_1_AD,
        label = "1080p (Full HD)",
        resolution = "1080x1920",
        isFree = false,
        requiredAdsCount = 1,
        lockReasonIndonesian = "LOCKED Nonton Iklan (1 Ad)"
    ),
    "4K" to QualityGateRule(
        tier = QualityTier.LOCKED_2_ADS,
        label = "1440p / 4K (Original)",
        resolution = "3840x2160",
        isFree = false,
        requiredAdsCount = 2,
        lockReasonIndonesian = "LOCKED Interstitial + Rewarded (2 Ads)"
    )
)

fun getQualityRequirement(qualityLabel: String): QualityGateRule {
    val tier = QualityTier.fromLabel(qualityLabel)
    return when (tier) {
        QualityTier.LOCKED_2_ADS -> QUALITY_GATE_RULES["4K"]!!
        QualityTier.LOCKED_1_AD -> QUALITY_GATE_RULES["1080p"]!!
        QualityTier.FREE -> QUALITY_GATE_RULES["720p"]!!
    }
}
