package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CrossPromoApp
import com.example.model.CrossPromoConfig
import com.example.ui.theme.*

fun openWebUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open web link: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun openPlayStoreOrMarket(context: Context, packageName: String) {
    try {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(marketIntent)
    } catch (_: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open Play Store: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * MoreAppsDialog:
 * Linear/Raycast premium glassmorphic modal
 * Pure iconography, no cheesy emojis
 */
@Composable
fun MoreAppsDialog(
    onDismiss: (dontShowToday: Boolean) -> Unit,
    currentAppId: String = CrossPromoConfig.CURRENT_APP_ID
) {
    val context = LocalContext.current
    var dontShowToday by remember { mutableStateOf(false) }
    val otherApps = remember(currentAppId) { CrossPromoConfig.getOtherApps(currentAppId) }

    Dialog(
        onDismissRequest = { onDismiss(dontShowToday) },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PureBlack,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("more_apps_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "More Applications",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = TextPureWhite
                        )
                    }

                    IconButton(
                        onClick = { onDismiss(dontShowToday) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_more_apps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Download ultra HD streams from other supported social networks with zero watermark.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // 3 App Cards (other apps only)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    otherApps.forEach { app ->
                        CrossPromoAppCard(
                            app = app,
                            onOpenWeb = { openWebUrl(context, app.webUrl) },
                            onDownloadApk = { openPlayStoreOrMarket(context, app.packageName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox: "Do not show again today"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { dontShowToday = !dontShowToday }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowToday,
                        onCheckedChange = { dontShowToday = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonBlue,
                            uncheckedColor = TextSecondary,
                            checkmarkColor = PureBlack
                        ),
                        modifier = Modifier.testTag("dont_show_today_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Do not show again today",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onDismiss(dontShowToday) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Continue Using ALLVID",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PureBlack
                    )
                }
            }
        }
    }
}

@Composable
fun CrossPromoAppCard(
    app: CrossPromoApp,
    onOpenWeb: () -> Unit,
    onDownloadApk: () -> Unit
) {
    val brandColor = try {
        Color(android.graphics.Color.parseColor(app.hexColor))
    } catch (_: Exception) {
        NeonBlue
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GlassSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(brandColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.take(1),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPureWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x222EE59D))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "FREE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = AccentGreen
                            )
                        }
                    }
                    Text(
                        text = app.desc,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenWeb,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        tint = PureBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Open Web",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureBlack
                    )
                }

                OutlinedButton(
                    onClick = onDownloadApk,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shop,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Get App",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * Permanent Footer Section: "More Apps From Us"
 */
@Composable
fun MoreAppsFooterSection(
    currentAppId: String = CrossPromoConfig.CURRENT_APP_ID,
    onAppClick: ((CrossPromoApp) -> Unit)? = null
) {
    val context = LocalContext.current
    val otherApps = remember(currentAppId) { CrossPromoConfig.getOtherApps(currentAppId) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("footer_more_apps_section")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "More Platforms From Us",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPureWhite
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                otherApps.forEach { app ->
                    val brandColor = try {
                        Color(android.graphics.Color.parseColor(app.hexColor))
                    } catch (_: Exception) {
                        NeonBlue
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (onAppClick != null) {
                                    onAppClick(app)
                                } else {
                                    openWebUrl(context, app.webUrl)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(brandColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Free",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                    }
                }
            }
        }
    }
}
