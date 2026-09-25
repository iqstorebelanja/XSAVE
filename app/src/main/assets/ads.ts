/**
 * ads.ts - AdMob Configuration & Quality Gate Rules for XSave (com.xsave.downloader)
 * 
 * Quality Gate:
 * - 720p (HD) = FREE
 * - 1080p (Full HD) = LOCKED (Nonton 1 Iklan / Rewarded Ad 1x)
 * - 1440p / 4K (Original) = LOCKED (Nonton 2 Iklan / Rewarded Ad 2x)
 */

export interface AdMobConfig {
  appId: string;
  bannerAdUnitId: string;
  interstitialAdUnitId: string;
  rewardedAdUnitId: string;
  interstitialFrequencyDownloads: number; // e.g. every 2 downloads
}

export enum QualityTier {
  SD_480P = '480p',
  HD_720P = '720p',
  FHD_1080P = '1080p',
  UHD_4K = '4K'
}

export interface QualityGateRule {
  tier: QualityTier;
  label: string;
  resolution: string;
  isFree: boolean;
  requiredAdsCount: number; // 0 = Free, 1 = 1 Ad, 2 = 2 Ads
  lockReasonIndonesian: string;
}

export const ADMOB_TEST_CONFIG: AdMobConfig = {
  appId: 'ca-app-pub-3940256099942544~3347511713',
  bannerAdUnitId: 'ca-app-pub-3940256099942544/6300978111',
  interstitialAdUnitId: 'ca-app-pub-3940256099942544/1033173712',
  rewardedAdUnitId: 'ca-app-pub-3940256099942544/5224354917',
  interstitialFrequencyDownloads: 2,
};

export const QUALITY_GATE_RULES: Record<string, QualityGateRule> = {
  '480p': {
    tier: QualityTier.SD_480P,
    label: '480p (SD)',
    resolution: '480x854',
    isFree: true,
    requiredAdsCount: 0,
    lockReasonIndonesian: 'GRATIS'
  },
  '720p': {
    tier: QualityTier.HD_720P,
    label: '720p (HD)',
    resolution: '720x1280',
    isFree: true,
    requiredAdsCount: 0,
    lockReasonIndonesian: 'GRATIS'
  },
  '1080p': {
    tier: QualityTier.FHD_1080P,
    label: '1080p (Full HD)',
    resolution: '1080x1920',
    isFree: false,
    requiredAdsCount: 1,
    lockReasonIndonesian: 'LOCKED Nonton Iklan (1 Ad)'
  },
  '4K': {
    tier: QualityTier.UHD_4K,
    label: '1440p / 4K (Original)',
    resolution: '3840x2160',
    isFree: false,
    requiredAdsCount: 2,
    lockReasonIndonesian: 'LOCKED 2 Iklan (2 Ads)'
  }
};

export function getQualityRequirement(qualityLabel: string): QualityGateRule {
  if (qualityLabel.contains('4K') || qualityLabel.contains('1440p') || qualityLabel.contains('2160p')) {
    return QUALITY_GATE_RULES['4K'];
  }
  if (qualityLabel.contains('1080p')) {
    return QUALITY_GATE_RULES['1080p'];
  }
  if (qualityLabel.contains('720p')) {
    return QUALITY_GATE_RULES['720p'];
  }
  return QUALITY_GATE_RULES['480p'];
}
