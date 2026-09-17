package com.widget.themes.controlcenter.core.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ViewTracker {
    private const val TAG = "ViewTracker"
    private val scope = CoroutineScope(Dispatchers.IO)

    fun track(itemType: String, itemId: String) {
        if (itemId.isEmpty()) return
        scope.launch {
            try {
                val params = mapOf(
                    "itemType" to itemType,
                    "itemId" to itemId
                )
                ApiClient.post<ApiResponse<Map<String, String>>>("api/v1/track-view", queryParams = params)
                Log.d(TAG, "Tracked view for $itemType: $itemId")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to track view for $itemType $itemId: ${e.message}")
            }
        }
    }
}
