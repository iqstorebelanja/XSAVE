package com.example.model

data class CrossPromoApp(
    val id: String,
    val name: String,
    val desc: String,
    val webUrl: String,
    val packageName: String,
    val hexColor: String
)

object CrossPromoConfig {
    val ALL_APPS = listOf(
        CrossPromoApp(
            id = "ytsave",
            name = "YTSave",
            desc = "YouTube Downloader",
            webUrl = "https://ytsave.vercel.app",
            packageName = "com.ytsave.downloader",
            hexColor = "#FF0000"
        ),
        CrossPromoApp(
            id = "savetok",
            name = "SaveTok",
            desc = "TikTok No Watermark",
            webUrl = "https://savetok.vercel.app",
            packageName = "com.savetok.downloader",
            hexColor = "#FE2C55"
        ),
        CrossPromoApp(
            id = "reelssave",
            name = "ReelsSave",
            desc = "IG Reels & Story",
            webUrl = "https://reelssave.vercel.app",
            packageName = "com.reelssave.downloader",
            hexColor = "#DD2A7B"
        ),
        CrossPromoApp(
            id = "xsave",
            name = "XSave",
            desc = "X Video Downloader",
            webUrl = "https://xsave.vercel.app",
            packageName = "com.xsave.downloader",
            hexColor = "#1DA1F2"
        )
    )

    const val CURRENT_APP_ID = "xsave"

    fun getOtherApps(currentId: String = CURRENT_APP_ID): List<CrossPromoApp> {
        return ALL_APPS.filter { it.id != currentId }
    }
}
