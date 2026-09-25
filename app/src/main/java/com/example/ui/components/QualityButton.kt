package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdsConfig
import com.example.ads.AdsManager
import com.example.model.QualityTier
import com.example.model.VideoVariant
import com.example.ui.theme.*

/**
 * QualityButton Component:
 * Uploaded component ported to Compose for all download action buttons:
 * - 720p: FREE (Shows daily limit 3x indicator)
 * - 1080p: 1 Rewarded Ad (Indicates 24h unlock state)
 * - 1440p / 4K: Interstitial + Rewarded (2 Ads, indicates 24h unlock state)
 */
@Composable
fun QualityButton(
    variant: VideoVariant,
    isSelected: Boolean,
    is1080pUnlocked: Boolean,
    is4KUnlocked: Boolean,
    isVipActive: Boolean = false,
    daily720pCount: Int = 0,
    adsWatchedFor4K: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tier = variant.tier
    val isLocked = when (tier) {
        QualityTier.FREE -> !isVipActive && daily720pCount >= AdsConfig.FREE_720P_DAILY_LIMIT
        QualityTier.LOCKED_1_AD -> !isVipActive && !is1080pUnlocked
        QualityTier.LOCKED_2_ADS -> !isVipActive && !is4KUnlocked
    }

    val isUnlockedPaid = when (tier) {
        QualityTier.FREE -> false
        QualityTier.LOCKED_1_AD -> is1080pUnlocked || isVipActive
        QualityTier.LOCKED_2_ADS -> is4KUnlocked || isVipActive
    }

    val resolutionLabel = when {
        variant.qualityLabel.contains("4K", ignoreCase = true) || variant.resolution.startsWith("3840") -> "4K UHD"
        variant.qualityLabel.contains("1080", ignoreCase = true) || variant.resolution.startsWith("1080") -> "1080p FHD"
        variant.qualityLabel.contains("720", ignoreCase = true) || variant.resolution.startsWith("720") -> "720p HD"
        variant.qualityLabel.contains("480", ignoreCase = true) || variant.resolution.startsWith("480") -> "480p SD"
        else -> variant.qualityLabel
    }

    val requirementBadge = when (tier) {
        QualityTier.FREE -> {
            if (isVipActive) "VIP UNLIMITED"
            else if (daily720pCount >= AdsConfig.FREE_720P_DAILY_LIMIT) "LIMIT (1 AD)"
            else "FREE (${daily720pCount}/${AdsConfig.FREE_720P_DAILY_LIMIT})"
        }
        QualityTier.LOCKED_1_AD -> {
            if (isVipActive || is1080pUnlocked) "UNLOCKED (24H)"
            else "1 REWARDED AD"
        }
        QualityTier.LOCKED_2_ADS -> {
            if (isVipActive || is4KUnlocked) "UNLOCKED (24H)"
            else if (adsWatchedFor4K == 1) "1/2 ADS"
            else "INTERSTITIAL + REWARDED"
        }
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) GlassSurfaceElevated else GlassSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) NeonBlue else GlassBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("quality_button_${variant.tier.code}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) NeonBlue else GlassSurfaceElevated
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else (if (isUnlockedPaid) Icons.Default.Check else Icons.Default.Download),
                        contentDescription = null,
                        tint = if (isSelected) PureBlack else (if (isLocked) TextSecondary else NeonBlue),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = resolutionLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonBlue else TextPureWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${variant.resolution})",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    if (variant.estimatedSizeBytes > 0) {
                        val mb = variant.estimatedSizeBytes / (1024 * 1024)
                        Text(
                            text = "~${mb} MB • MP4 Video",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Right Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            isUnlockedPaid -> Color(0x222EE59D)
                            isLocked -> Color(0x18FFFFFF)
                            else -> Color(0x222EE59D)
                        }
                    )
                    .border(
                        1.dp,
                        when {
                            isUnlockedPaid -> AccentGreen
                            isLocked -> GlassBorder
                            else -> AccentGreen
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = requirementBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        isUnlockedPaid -> AccentGreen
                        isLocked -> TextSecondary
                        else -> AccentGreen
                    }
                )
            }
        }
    }
}
