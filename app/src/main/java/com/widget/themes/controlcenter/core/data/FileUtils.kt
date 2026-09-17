package com.widget.themes.controlcenter.core.data

import android.content.Context

object FileUtils {
    fun loadJsonFromAsset(context: Context, filePath: String): String {
        return try {
            context.assets.open(filePath).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }
}
