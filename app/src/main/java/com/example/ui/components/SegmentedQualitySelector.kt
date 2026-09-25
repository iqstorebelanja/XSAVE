package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import com.example.model.QualityTier
import com.example.model.VideoVariant
import com.example.ui.theme.*

/**
 * YouTube-style Segmented Control Quality Selector:
 * [720p FREE]  [1080p 🔒 Ad]  [4K 🔒 2 Ads]
 * Active state: Neon Blue #00D1FF background / glowing border
 */
@Composable
fun SegmentedQualitySelector(
    variants: List<VideoVariant>,
    selectedVariant: VideoVariant?,
    is1080pUnlocked: Boolean,
    is4KUnlocked: Boolean,
    onVariantSelected: (VideoVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    if (variants.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(8.dp)
            .testTag("segmented_quality_selector")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x10FFFFFF))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            variants.forEach { variant ->
                val isSelected = selectedVariant?.url == variant.url

                val isLocked = when (variant.tier) {
                    QualityTier.FREE -> false
                    QualityTier.LOCKED_1_AD -> !is1080pUnlocked
                    QualityTier.LOCKED_2_ADS -> !is4KUnlocked
                }

                val tierLabel = when (variant.tier) {
                    QualityTier.FREE -> "FREE"
                    QualityTier.LOCKED_1_AD -> if (is1080pUnlocked) "UNLOCKED" else "1 Ad"
                    QualityTier.LOCKED_2_ADS -> if (is4KUnlocked) "UNLOCKED" else "2 Ads"
                }

                val resolutionShort = when {
                    variant.qualityLabel.contains("4K", ignoreCase = true) || variant.resolution.startsWith("3840") -> "4K"
                    variant.qualityLabel.contains("1080", ignoreCase = true) || variant.resolution.startsWith("1080") -> "1080p"
                    variant.qualityLabel.contains("720", ignoreCase = true) || variant.resolution.startsWith("720") -> "720p"
                    variant.qualityLabel.contains("480", ignoreCase = true) || variant.resolution.startsWith("480") -> "480p"
                    else -> variant.qualityLabel.substringBefore(" ")
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) NeonBlue else Color.Transparent
                        )
                        .clickable { onVariantSelected(variant) }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("segment_${resolutionShort}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = resolutionShort,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) PureBlack else TextPrimary
                            )

                            if (isLocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = if (isSelected) PureBlack else TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else if (!isSelected && (variant.tier != QualityTier.FREE)) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Unlocked",
                                    tint = AccentGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tierLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSelected -> PureBlack.copy(alpha = 0.8f)
                                variant.tier == QualityTier.FREE -> AccentGreen
                                isLocked -> TextSecondary
                                else -> AccentGreen
                            }
                        )
                    }
                }
            }
        }
    }
}
