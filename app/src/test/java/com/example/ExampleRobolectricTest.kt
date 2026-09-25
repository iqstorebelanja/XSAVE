package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdsManager
import com.example.model.CrossPromoConfig
import com.example.model.QualityTier
import com.example.model.VideoVariant
import com.example.network.TwitterExtractor
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        AdsManager.initialize(context)
        AdsManager.resetUnlocks()
        AdsManager.resetDownloadCounter()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("XSave", appName)
    }

    @Test
    fun `quality gate rules verification - 720p is free, 1080p and 4K are locked`() {
        val freeVariant = VideoVariant(
            qualityLabel = "720p (HD)",
            resolution = "720x1280",
            bitrate = 1500000L,
            url = "https://example.com/720.mp4",
            tier = QualityTier.FREE
        )
        val locked1080p = VideoVariant(
            qualityLabel = "1080p (Full HD)",
            resolution = "1080x1920",
            bitrate = 3200000L,
            url = "https://example.com/1080.mp4",
            tier = QualityTier.LOCKED_1_AD
        )
        val locked4K = VideoVariant(
            qualityLabel = "1440p / 4K (Original)",
            resolution = "3840x2160",
            bitrate = 12000000L,
            url = "https://example.com/4k.mp4",
            tier = QualityTier.LOCKED_2_ADS
        )

        assertTrue(freeVariant.isFree)
        assertFalse(freeVariant.isLocked1080p)
        assertFalse(freeVariant.isLocked4K)

        assertFalse(locked1080p.isFree)
        assertTrue(locked1080p.isLocked1080p)
        assertFalse(locked1080p.isLocked4K)

        assertFalse(locked4K.isFree)
        assertFalse(locked4K.isLocked1080p)
        assertTrue(locked4K.isLocked4K)
    }

    @Test
    fun `quality gate unlocking flow - 1080p with 1 ad, 4K with 2 ads`() {
        assertFalse(AdsManager.isTierUnlocked(QualityTier.LOCKED_1_AD))
        assertFalse(AdsManager.isTierUnlocked(QualityTier.LOCKED_2_ADS))

        var unlocked1080p = false
        AdsManager.requestRewardedUnlock(QualityTier.LOCKED_1_AD) {
            unlocked1080p = true
        }
        AdsManager.onRewardedAdCompleted()
        assertTrue(unlocked1080p)
        assertTrue(AdsManager.is1080pUnlocked.value)
        assertTrue(AdsManager.isTierUnlocked(QualityTier.LOCKED_1_AD))

        assertFalse(AdsManager.is4KUnlocked.value)

        var unlocked4K = false
        AdsManager.requestRewardedUnlock(QualityTier.LOCKED_2_ADS) {
            unlocked4K = true
        }
        AdsManager.onRewardedAdCompleted()
        assertEquals(1, AdsManager.adsWatchedFor4K.value)
        assertFalse(unlocked4K)
        assertFalse(AdsManager.is4KUnlocked.value)

        AdsManager.requestRewardedUnlock(QualityTier.LOCKED_2_ADS) {
            unlocked4K = true
        }
        AdsManager.onRewardedAdCompleted()
        assertEquals(2, AdsManager.adsWatchedFor4K.value)
        assertTrue(unlocked4K)
        assertTrue(AdsManager.is4KUnlocked.value)
        assertTrue(AdsManager.isTierUnlocked(QualityTier.LOCKED_2_ADS))
    }

    @Test
    fun `interstitial triggers every 2 downloads and cross promo triggers on 2nd completion and 3rd download`() {
        // Download 1
        var ad1Closed = false
        val triggered1 = AdsManager.onDownloadTriggered { ad1Closed = true }
        assertFalse(triggered1)
        assertTrue(ad1Closed)
        assertEquals(1, AdsManager.downloadCount.value)

        // Download 2
        var ad2Closed = false
        val triggered2 = AdsManager.onDownloadTriggered { ad2Closed = true }
        assertTrue(triggered2) // Interstitial triggered
        assertEquals(2, AdsManager.downloadCount.value)
        assertTrue(AdsManager.showInterstitial.value)

        AdsManager.dismissInterstitial()
        assertTrue(ad2Closed)
        assertFalse(AdsManager.showInterstitial.value)

        // 2nd download finished completion trigger: shows MoreApps popup
        AdsManager.onDownloadCompleted()
        assertTrue(AdsManager.showMoreApps.value)
        AdsManager.dismissMoreApps(dontShowToday = false)
        assertFalse(AdsManager.showMoreApps.value)

        // Download 3: before show AdMob interstitial, show MoreApps modal dulu 1x
        var ad3Closed = false
        val triggered3 = AdsManager.onDownloadTriggered { ad3Closed = true }
        assertTrue(triggered3)
        assertTrue(AdsManager.showMoreApps.value)
        AdsManager.dismissMoreApps(dontShowToday = false)
        assertFalse(AdsManager.showMoreApps.value)
    }

    @Test
    fun `cross promo config excludes current app and includes other 3 apps`() {
        val otherApps = CrossPromoConfig.getOtherApps("xsave")
        assertEquals(3, otherApps.size)
        assertFalse(otherApps.any { it.id == "xsave" })
        assertTrue(otherApps.any { it.id == "ytsave" })
        assertTrue(otherApps.any { it.id == "savetok" })
        assertTrue(otherApps.any { it.id == "reelssave" })

        val ytApps = CrossPromoConfig.getOtherApps("ytsave")
        assertEquals(3, ytApps.size)
        assertFalse(ytApps.any { it.id == "ytsave" })
        assertTrue(ytApps.any { it.id == "xsave" })
        assertTrue(ytApps.any { it.id == "savetok" })
        assertTrue(ytApps.any { it.id == "reelssave" })
    }

    @Test
    fun `sample tweets extractor contains all quality tiers including 4K original`() {
        val sample = TwitterExtractor.getCuratedSampleTweet("sample_spacex")
        assertNotNull(sample)
        val variants = sample!!.videos.first().variants
        assertTrue(variants.any { it.tier == QualityTier.LOCKED_2_ADS })
        assertTrue(variants.any { it.tier == QualityTier.LOCKED_1_AD })
        assertTrue(variants.any { it.tier == QualityTier.FREE })
    }
}
