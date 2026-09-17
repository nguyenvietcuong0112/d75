package com.widget.themes.controlcenter.core.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {

    // Preference File Names
    const val PREFS_DEFAULT = "app_preferences"
    const val PREFS_ICON_UNLOCK = "icon_unlock_prefs"
    const val PREFS_KEYBOARD = "keyboard_prefs"
    const val PREFS_WIDGET = "widget_prefs"
    const val PREFS_CHARGING = "charging_prefs"

    // Key Constants - Icon Unlock
    const val KEY_UNLOCKED_ICON_PREFIX = "unlocked_icon_"
    const val KEY_UNLOCKED_PACK_PREFIX = "unlocked_pack_"

    // Key Constants - General App
    const val KEY_FIRST_LAUNCH = "is_first_launch"
    const val KEY_RATED_APP = "is_rated_app"

    // Key Constants - Coins & App
    const val KEY_USER_COINS = "user_coins"

    // Key Constants - Charging
    const val KEY_APPLIED_CHARGING_FOLDER = "applied_charging_folder"

    // Key Constants - Widget
    const val KEY_BG_PATH_PREFIX = "bg_path_"
    const val KEY_WIDGET_TYPE_PREFIX = "widget_type_"

    fun getPrefs(context: Context, name: String = PREFS_DEFAULT): SharedPreferences {
        return context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    // ==========================================
    // 1. Icon Unlock Management
    // ==========================================

    fun isIconUnlocked(context: Context, itemId: String, themeId: String? = null): Boolean {
        val prefs = getPrefs(context, PREFS_ICON_UNLOCK)
        if (themeId != null && prefs.getBoolean("$KEY_UNLOCKED_PACK_PREFIX$themeId", false)) {
            return true
        }
        return prefs.getBoolean("$KEY_UNLOCKED_ICON_PREFIX$itemId", false)
    }

    fun setIconUnlocked(context: Context, itemId: String, unlocked: Boolean = true) {
        getPrefs(context, PREFS_ICON_UNLOCK).edit()
            .putBoolean("$KEY_UNLOCKED_ICON_PREFIX$itemId", unlocked)
            .apply()
    }

    fun setIconsUnlocked(context: Context, itemIds: List<String>, unlocked: Boolean = true) {
        val editor = getPrefs(context, PREFS_ICON_UNLOCK).edit()
        for (id in itemIds) {
            editor.putBoolean("$KEY_UNLOCKED_ICON_PREFIX$id", unlocked)
        }
        editor.apply()
    }

    fun setIconPackUnlocked(context: Context, themeId: String, unlocked: Boolean = true) {
        getPrefs(context, PREFS_ICON_UNLOCK).edit()
            .putBoolean("$KEY_UNLOCKED_PACK_PREFIX$themeId", unlocked)
            .apply()
    }

    fun isIconPackUnlocked(context: Context, themeId: String): Boolean {
        return getPrefs(context, PREFS_ICON_UNLOCK).getBoolean("$KEY_UNLOCKED_PACK_PREFIX$themeId", false)
    }

    // ==========================================
    // 2. User Coins Management
    // ==========================================

    fun getUserCoins(context: Context, defaultCoins: Int = 100): Int {
        return getPrefs(context, PREFS_DEFAULT).getInt(KEY_USER_COINS, defaultCoins)
    }

    fun setUserCoins(context: Context, coins: Int) {
        getPrefs(context, PREFS_DEFAULT).edit().putInt(KEY_USER_COINS, coins).apply()
    }

    fun addCoins(context: Context, amount: Int): Int {
        val current = getUserCoins(context)
        val updated = current + amount
        setUserCoins(context, updated)
        return updated
    }

    // ==========================================
    // 3. Widget Management
    // ==========================================

    fun getWidgetBgPath(context: Context, cleanId: String, type: String, size: String): String? {
        return getPrefs(context, PREFS_WIDGET).getString("${KEY_BG_PATH_PREFIX}${cleanId}_${type}_$size", null)
    }

    fun setWidgetBgPath(context: Context, cleanId: String, type: String, size: String, fileName: String) {
        getPrefs(context, PREFS_WIDGET).edit()
            .putString("${KEY_BG_PATH_PREFIX}${cleanId}_${type}_$size", fileName)
            .apply()
    }

    fun getWidgetType(context: Context, widgetId: Int, defaultType: String = ""): String {
        return getPrefs(context, PREFS_WIDGET).getString("${KEY_WIDGET_TYPE_PREFIX}$widgetId", defaultType) ?: defaultType
    }

    fun setWidgetType(context: Context, widgetId: Int, type: String) {
        getPrefs(context, PREFS_WIDGET).edit()
            .putString("${KEY_WIDGET_TYPE_PREFIX}$widgetId", type)
            .apply()
    }

    fun getCountdownData(context: Context, widgetId: Int): Pair<String, String> {
        val title = getPrefs(context, PREFS_WIDGET).getString("countdown_title_$widgetId", "My Birthday") ?: "My Birthday"
        val date = getPrefs(context, PREFS_WIDGET).getString("countdown_date_$widgetId", "2027-06-04") ?: "2027-06-04"
        return Pair(title, date)
    }

    fun setCountdownData(context: Context, widgetId: Int, title: String, date: String) {
        getPrefs(context, PREFS_WIDGET).edit()
            .putString("countdown_title_$widgetId", title)
            .putString("countdown_date_$widgetId", date)
            .apply()
    }

    fun getChecklistData(context: Context, widgetId: Int): List<Pair<String, Boolean>> {
        val defaultTasks = listOf(
            Pair("Buy strawberries", true),
            Pair("Bake a cake", false),
            Pair("Call mom", false),
            Pair("Water the flowers", false)
        )
        val raw = getPrefs(context, PREFS_WIDGET).getString("checklist_data_$widgetId", null)
            ?: return defaultTasks
        return try {
            raw.split("||").map {
                val parts = it.split("::")
                Pair(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "false" }.toBoolean())
            }
        } catch (e: Exception) {
            defaultTasks
        }
    }

    fun setChecklistData(context: Context, widgetId: Int, tasks: List<Pair<String, Boolean>>) {
        val encoded = tasks.joinToString("||") { "${it.first}::${it.second}" }
        getPrefs(context, PREFS_WIDGET).edit()
            .putString("checklist_data_$widgetId", encoded)
            .apply()
    }

    fun getNoteData(context: Context, widgetId: Int): String {
        return getPrefs(context, PREFS_WIDGET).getString(
            "note_data_$widgetId",
            "Good morning,\n sunshine! ☀️🍓\nA brand-new day is here, so take a deep breath, smile, and enjoy all the little moments."
        ) ?: "Good morning,\n sunshine! ☀️🍓\nA brand-new day is here, so take a deep breath, smile, and enjoy all the little moments."
    }

    fun setNoteData(context: Context, widgetId: Int, note: String) {
        getPrefs(context, PREFS_WIDGET).edit()
            .putString("note_data_$widgetId", note)
            .apply()
    }

    // ==========================================
    // 4. Charging Management
    // ==========================================

    fun getAppliedChargingFolder(context: Context, defaultFolder: String = "charging/charging_1"): String {
        return getPrefs(context, PREFS_CHARGING).getString(KEY_APPLIED_CHARGING_FOLDER, defaultFolder) ?: defaultFolder
    }

    fun setAppliedChargingFolder(context: Context, folder: String) {
        getPrefs(context, PREFS_CHARGING).edit().putString(KEY_APPLIED_CHARGING_FOLDER, folder).apply()
    }

    // ==========================================
    // 5. Generic Key-Value Helpers
    // ==========================================

    fun putString(context: Context, key: String, value: String, prefName: String = PREFS_DEFAULT) {
        getPrefs(context, prefName).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, defaultValue: String? = null, prefName: String = PREFS_DEFAULT): String? {
        return getPrefs(context, prefName).getString(key, defaultValue)
    }

    fun putBoolean(context: Context, key: String, value: Boolean, prefName: String = PREFS_DEFAULT) {
        getPrefs(context, prefName).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false, prefName: String = PREFS_DEFAULT): Boolean {
        return getPrefs(context, prefName).getBoolean(key, defaultValue)
    }

    fun putInt(context: Context, key: String, value: Int, prefName: String = PREFS_DEFAULT) {
        getPrefs(context, prefName).edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0, prefName: String = PREFS_DEFAULT): Int {
        return getPrefs(context, prefName).getInt(key, defaultValue)
    }

    fun putLong(context: Context, key: String, value: Long, prefName: String = PREFS_DEFAULT) {
        getPrefs(context, prefName).edit().putLong(key, value).apply()
    }

    fun getLong(context: Context, key: String, defaultValue: Long = 0L, prefName: String = PREFS_DEFAULT): Long {
        return getPrefs(context, prefName).getLong(key, defaultValue)
    }

    fun remove(context: Context, key: String, prefName: String = PREFS_DEFAULT) {
        getPrefs(context, prefName).edit().remove(key).apply()
    }

    fun clear(context: Context, prefName: String = PREFS_DEFAULT) {
        getPrefs(context, prefName).edit().clear().apply()
    }
}
