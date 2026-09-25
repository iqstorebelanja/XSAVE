package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Top Bar:
 * Left: ALLVID logo
 * Center: "20+ Platforms Supported"
 * Right: "VIP $29/mo" premium toggle switch
 */
@Composable
fun AllVidTopBar(
    isVipActive: Boolean,
    onVipToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0x1A000000),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .testTag("allvid_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: ALLVID logo with subtle gradient neon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(NeonBlue, Color(0xFF0055FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = PureBlack,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ALLVID",
                            color = TextPureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x1800D1FF))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = NeonBlue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Center badge: "20+ Platforms Supported"
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GlassSurfaceElevated)
                    .border(1.dp, GlassBorder, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "20+ Platforms",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Right: VIP $29/mo premium toggle switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isVipActive) Color(0x2200D1FF) else GlassSurface
                    )
                    .border(
                        1.dp,
                        if (isVipActive) NeonBlueDark else GlassBorder,
                        CircleShape
                    )
                    .clickable { onVipToggle(!isVipActive) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("vip_toggle_switch")
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "VIP",
                    tint = if (isVipActive) NeonBlue else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = if (isVipActive) "VIP Active" else "$29/mo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVipActive) NeonBlue else TextPrimary
                )

                Switch(
                    checked = isVipActive,
                    onCheckedChange = onVipToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = NeonBlue,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    ),
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}
