package com.widget.themes.controlcenter.core.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Telephony

data class DefaultAppInfo(
    val id: String,
    val name: String,
    val packageName: String
)

data class MatchedAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

object AppItemData {
    val APPS = listOf(
        DefaultAppInfo("facebook", "Facebook", "com.facebook.katana"),
        DefaultAppInfo("instagram", "Instagram", "com.instagram.android"),
        DefaultAppInfo("tiktok", "TikTok", "com.zhiliaoapp.musically"),
        DefaultAppInfo("youtube", "YouTube", "com.google.android.youtube"),
        DefaultAppInfo("messenger", "Facebook Messenger", "com.facebook.orca"),
        DefaultAppInfo("fb_messenger", "Facebook Messenger", "com.facebook.orca"),
        DefaultAppInfo("facebook_messenger", "Facebook Messenger", "com.facebook.orca"),
        DefaultAppInfo("message", "Messages", "com.google.android.apps.messaging"),
        DefaultAppInfo("messages", "Messages", "com.google.android.apps.messaging"),
        DefaultAppInfo("sms", "Messages", "com.google.android.apps.messaging"),
        DefaultAppInfo("gmail", "Gmail", "com.google.android.gm"),
        DefaultAppInfo("chrome", "Chrome", "com.android.chrome"),
        DefaultAppInfo("phone", "Phone", "com.android.phone"),
        DefaultAppInfo("camera", "Camera", "com.android.camera"),
        DefaultAppInfo("settings", "Settings", "com.android.settings"),
        DefaultAppInfo("whatsapp", "WhatsApp", "com.whatsapp"),
        DefaultAppInfo("telegram", "Telegram", "org.telegram.messenger"),
        DefaultAppInfo("twitter", "Twitter", "com.twitter.android"),
        DefaultAppInfo("x", "X", "com.twitter.android"),
        DefaultAppInfo("snapchat", "Snapchat", "com.snapchat.android"),
        DefaultAppInfo("spotify", "Spotify", "com.spotify.music"),
        DefaultAppInfo("maps", "Maps", "com.google.android.apps.maps"),
        DefaultAppInfo("google_map", "Google Maps", "com.google.android.apps.maps"),
        DefaultAppInfo("google_maps", "Google Maps", "com.google.android.apps.maps"),
        DefaultAppInfo("linkedin", "LinkedIn", "com.linkedin.android"),
        DefaultAppInfo("amazon", "Amazon", "com.amazon.mShop.android.shopping"),
        DefaultAppInfo("shopee", "Shopee", "com.shopee.vn"),
        DefaultAppInfo("lazada", "Lazada", "com.lazada.android"),
        DefaultAppInfo("paypal", "PayPal", "com.paypal.android.p2pmobile"),
        DefaultAppInfo("google_wallet", "Google Wallet", "com.google.android.apps.walletnfcrel"),
        DefaultAppInfo("wallet", "Google Wallet", "com.google.android.apps.walletnfcrel"),
        DefaultAppInfo("netflix", "Netflix", "com.netflix.mediaclient"),
        DefaultAppInfo("yt_music", "YouTube Music", "com.google.android.apps.youtube.music"),
        DefaultAppInfo("youtube_music", "YouTube Music", "com.google.android.apps.youtube.music"),
        DefaultAppInfo("ytmusic", "YouTube Music", "com.google.android.apps.youtube.music"),
        DefaultAppInfo("play_games", "Google Play Games", "com.google.android.play.games"),
        DefaultAppInfo("games", "Google Play Games", "com.google.android.play.games"),
        DefaultAppInfo("gg_play_game", "Google Play Games", "com.google.android.play.games"),
        DefaultAppInfo("chatgpt", "ChatGPT", "com.openai.chatgpt"),
        DefaultAppInfo("chat_gpt", "ChatGPT", "com.openai.chatgpt"),
        DefaultAppInfo("outlook", "Outlook", "com.microsoft.office.outlook"),
        DefaultAppInfo("contacts", "Contacts", "com.google.android.contacts"),
        DefaultAppInfo("phonebook", "Contacts", "com.google.android.contacts"),
        DefaultAppInfo("files", "Files", "com.google.android.documentsui"),
        DefaultAppInfo("file", "Files", "com.google.android.documentsui"),
        DefaultAppInfo("binance", "Binance", "com.binance.dev"),
        DefaultAppInfo("twitch", "Twitch", "tv.twitch.android.app"),
        DefaultAppInfo("zalo", "Zalo", "com.zing.zalo"),
        DefaultAppInfo("capcut", "CapCut", "com.lemon.lvoverseas"),
        DefaultAppInfo("pinterest", "Pinterest", "com.pinterest"),
        DefaultAppInfo("discord", "Discord", "com.discord")
    )
}

object AppMatcherHelper {

    private val KNOWN_APP_PACKAGES = mapOf(
        "tiktok" to listOf("com.ss.android.ugc.trill", "com.zhiliaoapp.musically", "com.ss.android.ugc.trill.go"),
        "facebook" to listOf("com.facebook.katana", "com.facebook.lite"),
        "messenger" to listOf("com.facebook.orca", "com.facebook.mlite"),
        "fb_messenger" to listOf("com.facebook.orca", "com.facebook.mlite"),
        "facebook_messenger" to listOf("com.facebook.orca", "com.facebook.mlite"),
        "instagram" to listOf("com.instagram.android", "com.instagram.lite"),
        "youtube" to listOf("com.google.android.youtube"),
        "youtube_music" to listOf("com.google.android.apps.youtube.music"),
        "yt_music" to listOf("com.google.android.apps.youtube.music"),
        "ytmusic" to listOf("com.google.android.apps.youtube.music"),
        "chrome" to listOf("com.android.chrome"),
        "gmail" to listOf("com.google.android.gm", "com.google.android.gm.lite"),
        "telegram" to listOf("org.telegram.messenger", "org.telegram.plus", "org.thunderdog.challegram"),
        "whatsapp" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "twitter" to listOf("com.twitter.android", "com.twitter.android.lite"),
        "x" to listOf("com.twitter.android", "com.twitter.android.lite"),
        "snapchat" to listOf("com.snapchat.android"),
        "spotify" to listOf("com.spotify.music", "com.spotify.lite"),
        "maps" to listOf("com.google.android.apps.maps", "com.google.android.apps.mapslite"),
        "google_map" to listOf("com.google.android.apps.maps", "com.google.android.apps.mapslite"),
        "google_maps" to listOf("com.google.android.apps.maps", "com.google.android.apps.mapslite"),
        "chatgpt" to listOf("com.openai.chatgpt"),
        "chat_gpt" to listOf("com.openai.chatgpt"),
        "netflix" to listOf("com.netflix.mediaclient"),
        "shopee" to listOf("com.shopee.vn", "com.shopee.ph", "com.shopee.id", "com.shopee.my", "com.shopee.sg", "com.shopee.th"),
        "lazada" to listOf("com.lazada.android"),
        "zalo" to listOf("com.zing.zalo"),
        "paypal" to listOf("com.paypal.android.p2pmobile"),
        "amazon" to listOf("com.amazon.mShop.android.shopping"),
        "linkedin" to listOf("com.linkedin.android"),
        "play_games" to listOf("com.google.android.play.games"),
        "games" to listOf("com.google.android.play.games"),
        "gg_play_game" to listOf("com.google.android.play.games"),
        "google_wallet" to listOf("com.google.android.apps.walletnfcrel"),
        "wallet" to listOf("com.google.android.apps.walletnfcrel"),
        "outlook" to listOf("com.microsoft.office.outlook"),
        "capcut" to listOf("com.lemon.lvoverseas"),
        "pinterest" to listOf("com.pinterest"),
        "discord" to listOf("com.discord"),
        "binance" to listOf("com.binance.dev"),
        "twitch" to listOf("tv.twitch.android.app")
    )

    fun resolveAppForIcon(
        context: Context,
        rawIconName: String,
        installedLauncherApps: List<ResolveInfo>
    ): MatchedAppInfo? {
        val pm = context.packageManager
        val cleanName = rawIconName.removePrefix("ic_").lowercase().trim()

        // 1. System Intent Matching (Phone, Camera, Gallery, SMS, Settings, Calculator, Calendar, Contacts, Files, Browser)
        val systemApp = resolveSystemApp(context, pm, cleanName, installedLauncherApps)
        if (systemApp != null) {
            return systemApp
        }

        // 2. Known Popular Apps Matching (Multi-Package Support)
        val candidatePackages = KNOWN_APP_PACKAGES[cleanName]
        if (candidatePackages != null) {
            for (pkg in candidatePackages) {
                val launcherMatch = installedLauncherApps.firstOrNull { it.activityInfo.packageName.equals(pkg, ignoreCase = true) }
                if (launcherMatch != null) {
                    return MatchedAppInfo(
                        packageName = launcherMatch.activityInfo.packageName,
                        appName = launcherMatch.loadLabel(pm).toString(),
                        icon = launcherMatch.loadIcon(pm)
                    )
                }
                // Fallback to getApplicationInfo if launchable
                try {
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        val appInfo = pm.getApplicationInfo(pkg, 0)
                        return MatchedAppInfo(
                            packageName = pkg,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            icon = pm.getApplicationIcon(appInfo)
                        )
                    }
                } catch (e: Exception) {
                    // Not installed
                }
            }
        }

        // 3. Fuzzy search in installed launcher apps by app label or package name
        val fuzzyMatch = installedLauncherApps.firstOrNull { app ->
            val label = app.loadLabel(pm).toString().lowercase()
            val pkg = app.activityInfo.packageName.lowercase()
            label.contains(cleanName) || cleanName.contains(label) || pkg.contains(".$cleanName")
        }
        if (fuzzyMatch != null) {
            return MatchedAppInfo(
                packageName = fuzzyMatch.activityInfo.packageName,
                appName = fuzzyMatch.loadLabel(pm).toString(),
                icon = fuzzyMatch.loadIcon(pm)
            )
        }

        return null
    }

    private fun resolveSystemApp(
        context: Context,
        pm: PackageManager,
        name: String,
        installedLauncherApps: List<ResolveInfo>
    ): MatchedAppInfo? {
        return when (name) {
            "phone", "call", "dialer" -> {
                resolveByIntent(pm, Intent(Intent.ACTION_DIAL), installedLauncherApps)
                    ?: resolveByPackages(pm, listOf("com.google.android.dialer", "com.samsung.android.dialer", "com.android.dialer"), installedLauncherApps)
            }
            "camera" -> {
                resolveByIntent(pm, Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), installedLauncherApps)
                    ?: resolveByIntent(pm, Intent(MediaStore.ACTION_IMAGE_CAPTURE), installedLauncherApps)
                    ?: resolveByPackages(pm, listOf("com.sec.android.app.camera", "com.google.android.GoogleCamera", "com.android.camera", "com.oneplus.camera"), installedLauncherApps)
            }
            "gallery", "photo", "photos" -> {
                resolveByPackages(pm, listOf("com.google.android.apps.photos", "com.sec.android.gallery3d", "com.miui.gallery", "com.android.gallery3d", "com.coloros.gallery3d"), installedLauncherApps)
                    ?: resolveByIntent(pm, Intent(Intent.ACTION_VIEW).apply { type = "image/*" }, installedLauncherApps)
            }
            "message", "messages", "sms" -> {
                val defaultSms = Telephony.Sms.getDefaultSmsPackage(context)
                if (defaultSms != null) {
                    val match = installedLauncherApps.firstOrNull { it.activityInfo.packageName.equals(defaultSms, ignoreCase = true) }
                    if (match != null) {
                        return MatchedAppInfo(match.activityInfo.packageName, match.loadLabel(pm).toString(), match.loadIcon(pm))
                    }
                }
                resolveByPackages(pm, listOf("com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms"), installedLauncherApps)
            }
            "contacts", "phonebook" -> {
                resolveByIntent(pm, Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI), installedLauncherApps)
                    ?: resolveByPackages(pm, listOf("com.google.android.contacts", "com.samsung.android.app.contacts", "com.android.contacts"), installedLauncherApps)
            }
            "setting", "settings" -> {
                resolveByIntent(pm, Intent(Settings.ACTION_SETTINGS), installedLauncherApps)
                    ?: resolveByPackages(pm, listOf("com.android.settings"), installedLauncherApps)
            }
            "calculator" -> {
                resolveByPackages(
                    pm,
                    listOf(
                        "com.google.android.calculator", "com.sec.android.app.popupcalculator",
                        "com.miui.calculator", "com.android.calculator2", "com.coloros.calculator",
                        "com.oppo.calculator", "com.oneplus.calculator", "com.asus.calculator"
                    ),
                    installedLauncherApps
                )
            }
            "calendar" -> {
                resolveByIntent(pm, Intent(Intent.ACTION_VIEW).setData(CalendarContract.CONTENT_URI), installedLauncherApps)
                    ?: resolveByPackages(
                        pm,
                        listOf(
                            "com.google.android.calendar", "com.samsung.android.calendar",
                            "com.android.calendar", "com.miui.calendar", "com.coloros.calendar",
                            "com.oppo.calendar", "com.oneplus.calendar"
                        ),
                        installedLauncherApps
                    )
            }
            "clock", "alarm", "timer" -> {
                resolveByIntent(pm, Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS), installedLauncherApps)
                    ?: resolveByPackages(
                        pm,
                        listOf(
                            "com.google.android.deskclock", "com.sec.android.app.clockpackage",
                            "com.android.deskclock", "com.miui.calculator", "com.coloros.alarmclock",
                            "com.oppo.alarmclock", "com.oneplus.deskclock", "com.huawei.alarmclock"
                        ),
                        installedLauncherApps
                    )
            }
            "download", "downloads" -> {
                resolveByPackages(
                    pm,
                    listOf(
                        "com.android.providers.downloads.ui", "com.google.android.documentsui",
                        "com.sec.android.app.myfiles", "com.google.android.apps.nbu.files"
                    ),
                    installedLauncherApps
                )
            }
            "music", "audio" -> {
                resolveByPackages(
                    pm,
                    listOf(
                        "com.google.android.apps.youtube.music", "com.spotify.music",
                        "com.sec.android.app.music", "com.miui.player", "com.android.music"
                    ),
                    installedLauncherApps
                )
            }
            "notes", "note", "memo" -> {
                resolveByPackages(
                    pm,
                    listOf(
                        "com.google.android.keep", "com.samsung.android.app.notes",
                        "com.miui.notes", "com.coloros.note", "com.nearme.note"
                    ),
                    installedLauncherApps
                )
            }
            "mail", "email", "gmail" -> {
                resolveByPackages(
                    pm,
                    listOf(
                        "com.google.android.gm", "com.samsung.android.email.provider",
                        "com.android.email", "com.microsoft.office.outlook"
                    ),
                    installedLauncherApps
                )
            }
            "weather" -> {
                resolveByPackages(
                    pm,
                    listOf(
                        "com.google.android.apps.weather", "com.sec.android.daemonapp",
                        "com.miui.weather2", "com.coloros.weather", "com.huawei.android.totemweather"
                    ),
                    installedLauncherApps
                )
            }
            "files", "file" -> {
                resolveByPackages(
                    pm,
                    listOf(
                        "com.google.android.apps.nbu.files", "com.sec.android.app.myfiles",
                        "com.mi.android.globalFileexplorer", "com.google.android.documentsui",
                        "com.android.documentsui"
                    ),
                    installedLauncherApps
                )
            }
            "chrome", "browser", "web" -> {
                resolveByPackages(pm, listOf("com.android.chrome"), installedLauncherApps)
                    ?: resolveByIntent(pm, Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com")), installedLauncherApps)
            }
            else -> null
        }
    }

    private fun resolveByIntent(
        pm: PackageManager,
        intent: Intent,
        installedLauncherApps: List<ResolveInfo>
    ): MatchedAppInfo? {
        val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfo != null && resolveInfo.activityInfo.packageName != "android" && resolveInfo.activityInfo.packageName != "com.google.android.apps.nexuslauncher") {
            val pkg = resolveInfo.activityInfo.packageName
            val launcherMatch = installedLauncherApps.firstOrNull { it.activityInfo.packageName.equals(pkg, ignoreCase = true) }
            if (launcherMatch != null) {
                return MatchedAppInfo(
                    packageName = launcherMatch.activityInfo.packageName,
                    appName = launcherMatch.loadLabel(pm).toString(),
                    icon = launcherMatch.loadIcon(pm)
                )
            }
            return MatchedAppInfo(
                packageName = pkg,
                appName = resolveInfo.loadLabel(pm).toString(),
                icon = resolveInfo.loadIcon(pm)
            )
        }
        return null
    }

    private fun resolveByPackages(
        pm: PackageManager,
        candidatePackages: List<String>,
        installedLauncherApps: List<ResolveInfo>
    ): MatchedAppInfo? {
        for (pkg in candidatePackages) {
            val launcherMatch = installedLauncherApps.firstOrNull { it.activityInfo.packageName.equals(pkg, ignoreCase = true) }
            if (launcherMatch != null) {
                return MatchedAppInfo(
                    packageName = launcherMatch.activityInfo.packageName,
                    appName = launcherMatch.loadLabel(pm).toString(),
                    icon = launcherMatch.loadIcon(pm)
                )
            }
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                return MatchedAppInfo(
                    packageName = pkg,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            } catch (e: Exception) {
                // Not found
            }
        }
        return null
    }
}
