package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
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
import com.example.ui.theme.*

/**
 * XSave Top Bar:
 * Authentic X.com style header:
 * Left: 𝕏 logo + XSave title
 * Center: PWA / Mobile-First badge
 * Right: VIP / Ad-Free toggle switch
 */
@Composable
fun XSaveTopBar(
    isVipActive: Boolean,
    onVipToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = XBlack,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, XCardBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .testTag("xsave_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: 𝕏 Logo and App Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(XDarkSurface)
                        .border(1.dp, XCardBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "𝕏",
                        color = TextPureWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "XSave",
                            color = TextPureWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.2).sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x221D9BF0))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PWA",
                                color = XBlue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "X (Twitter) Video Downloader",
                        color = XTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Right: VIP / Ad-Free toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isVipActive) Color(0x221D9BF0) else XDarkSurface)
                    .border(1.dp, if (isVipActive) XBlue else XCardBorder, CircleShape)
                    .clickable { onVipToggle(!isVipActive) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("vip_toggle_switch")
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "VIP",
                    tint = if (isVipActive) XBlue else XTextSecondary,
                    modifier = Modifier.size(14.dp)
                )

                Text(
                    text = if (isVipActive) "VIP Active" else "Ad-Free",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVipActive) XBlue else XTextPrimary
                )

                Switch(
                    checked = isVipActive,
                    onCheckedChange = onVipToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = XBlack,
                        checkedTrackColor = XBlue,
                        uncheckedThumbColor = XTextSecondary,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    ),
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}
