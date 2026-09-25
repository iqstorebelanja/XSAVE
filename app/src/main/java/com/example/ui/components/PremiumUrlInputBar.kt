package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class PlatformIconItem(
    val name: String,
    val shortLabel: String,
    val tagColor: Color
)

val SUPPORTED_PLATFORMS = listOf(
    PlatformIconItem("TikTok", "TT", Color(0xFFFE2C55)),
    PlatformIconItem("Instagram", "IG", Color(0xFFDD2A7B)),
    PlatformIconItem("X (Twitter)", "X", Color(0xFFFFFFFF)),
    PlatformIconItem("YouTube", "YT", Color(0xFFFF0000)),
    PlatformIconItem("Facebook", "FB", Color(0xFF1877F2)),
    PlatformIconItem("Threads", "TH", Color(0xFFFFFFFF)),
    PlatformIconItem("Reddit", "RD", Color(0xFFFF4500)),
    PlatformIconItem("Pinterest", "PIN", Color(0xFFE60023))
)

/**
 * Premium 64px Huge Rounded-Full URL Input with Animated Platform Icons
 * Styled like Linear / Raycast command bar with glassmorphism (bg-white/[0.03] border-white/[0.06])
 */
@Composable
fun PremiumUrlInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current

    // Animated cycling platform ticker inside input (TikTok, IG, X, YT scrolling)
    val infiniteTransition = rememberInfiniteTransition(label = "platformTicker")
    val tickerIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = SUPPORTED_PLATFORMS.size - 1,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SUPPORTED_PLATFORMS.size * 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tickerAnimation"
    )

    val currentPlatform = SUPPORTED_PLATFORMS[tickerIndex.coerceIn(0, SUPPORTED_PLATFORMS.size - 1)]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(CircleShape)
            .background(GlassSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (value.isNotEmpty()) NeonBlueDark else GlassBorder,
                shape = CircleShape
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated scrolling platform badge inside input
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .clip(CircleShape)
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, Color(0x22FFFFFF), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(currentPlatform.tagColor)
                    )
                    Text(
                        text = currentPlatform.name,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text input area with custom placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Paste any video URL...",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(NeonBlue),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { onSubmit() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("premium_url_input_field")
                )
            }

            // Action buttons on the right inside 64px rounded bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clear_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    // Quick paste button
                    IconButton(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                onValueChange(clipText.trim())
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("paste_url_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Primary extract button
                Button(
                    onClick = onSubmit,
                    enabled = !isLoading && value.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonBlue,
                        disabledContainerColor = Color(0x2200D1FF)
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("extract_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = PureBlack,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Extract",
                                color = PureBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PureBlack,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
