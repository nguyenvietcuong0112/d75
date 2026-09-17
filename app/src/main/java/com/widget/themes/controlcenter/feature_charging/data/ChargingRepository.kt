package com.widget.themes.controlcenter.feature_charging.data

import android.content.Context
import android.util.Log
import com.widget.themes.controlcenter.core.network.ApiClient
import com.widget.themes.controlcenter.core.network.ApiResponse
import com.widget.themes.controlcenter.core.network.CategoryDto
import com.widget.themes.controlcenter.core.network.ChargingDto
import com.widget.themes.controlcenter.core.network.PagedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChargingRepository(private val context: Context) {

    private val TAG = "ChargingRepository"
    private var cachedCategories: List<CategoryDto>? = null

    suspend fun getCategories(): List<CategoryDto> = withContext(Dispatchers.IO) {
        cachedCategories?.let { return@withContext it }
        try {
            val response = ApiClient.get<ApiResponse<List<CategoryDto>>>(
                "api/v1/categories",
                mapOf("type" to "charging")
            )
            val list = response?.data
            if (!list.isNullOrEmpty()) {
                cachedCategories = list
                return@withContext list
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCategories error: ${e.message}")
        }
        emptyList()
    }

    suspend fun getChargingAnimations(
        categoryId: String? = null,
        page: Int = 1,
        pageSize: Int = 30
    ): List<ChargingDto> = withContext(Dispatchers.IO) {
        try {
            val params = mutableMapOf(
                "page" to page.toString(),
                "page_size" to pageSize.toString()
            )
            if (!categoryId.isNullOrEmpty() && !categoryId.equals("all", ignoreCase = true)) {
                params["category_id"] = categoryId
            }

            val response = ApiClient.get<ApiResponse<PagedData<ChargingDto>>>("api/v1/charging", params)
            val items = response?.data?.items
            if (!items.isNullOrEmpty()) {
                return@withContext items
            }
        } catch (e: Exception) {
            Log.e(TAG, "getChargingAnimations error: ${e.message}")
        }
        emptyList()
    }

    suspend fun getChargingDetail(id: String): ChargingDto? = withContext(Dispatchers.IO) {
        try {
            val cleanId = id.substringAfterLast("/").trim()
            val response = ApiClient.get<ApiResponse<ChargingDto>>("api/v1/charging/$cleanId")
            return@withContext response?.data
        } catch (e: Exception) {
            Log.e(TAG, "getChargingDetail error: ${e.message}")
        }
        null
    }
}
