package com.widget.themes.controlcenter.core.network

import android.util.Log
import com.widget.themes.controlcenter.BuildConfig
import com.widget.themes.controlcenter.core.data.ResourceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ApiClient {
    @PublishedApi internal const val TAG = "ApiClient"
    @PublishedApi internal const val CONNECT_TIMEOUT_MS = 10000
    @PublishedApi internal const val READ_TIMEOUT_MS = 10000

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @PublishedApi
    internal fun getBaseUrl(): String {
        val configured = ResourceConfig.API_BASE_URL
        return if (configured.isNotEmpty()) configured.trimEnd('/') else "https://api.1teps.com/themes"
    }

    @PublishedApi
    internal fun buildUrlWithParams(endpoint: String, queryParams: Map<String, String?>): String {
        val base = getBaseUrl()
        val cleanEndpoint = endpoint.trimStart('/')
        val urlBuilder = StringBuilder()

        if (cleanEndpoint.startsWith("http://") || cleanEndpoint.startsWith("https://")) {
            urlBuilder.append(cleanEndpoint)
        } else {
            urlBuilder.append(base).append("/").append(cleanEndpoint)
        }

        val allParams = mutableMapOf<String, String>()
        // Default param1 = "1" and param2 = "1" per CMS testing configuration
        allParams["param1"] = "1"
        allParams["param2"] = "1"

        for ((k, v) in queryParams) {
            if (!v.isNullOrEmpty()) {
                allParams[k] = v
            }
        }

        val separator = if (urlBuilder.contains("?")) "&" else "?"
        val queryStr = allParams.entries.joinToString("&") { (k, v) ->
            "${URLEncoder.encode(k, "UTF-8")}=${URLEncoder.encode(v, "UTF-8")}"
        }

        if (queryStr.isNotEmpty()) {
            urlBuilder.append(separator).append(queryStr)
        }

        return urlBuilder.toString()
    }

    suspend inline fun <reified T> get(endpoint: String, queryParams: Map<String, String?> = emptyMap()): T? = withContext(Dispatchers.IO) {
        val fullUrl = buildUrlWithParams(endpoint, queryParams)
        val startTime = System.currentTimeMillis()
        Log.i(TAG, "--> GET $fullUrl")
        try {
            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "ThemeAppAndroid/${BuildConfig.VERSION_NAME}")
            }

            val responseCode = connection.responseCode
            val duration = System.currentTimeMillis() - startTime
            if (responseCode in 200..299) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                Log.i(TAG, "<-- 200 OK ($duration ms) GET $fullUrl\nResponse: $jsonStr")
                try {
                    val parsed = json.decodeFromString<T>(jsonStr)
                    return@withContext parsed
                } catch (parseEx: Exception) {
                    Log.e(TAG, "JSON Parse Error for GET $fullUrl: ${parseEx.message}\nRaw JSON: $jsonStr", parseEx)
                    null
                }
            } else {
                val errBody = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                } catch (e: Exception) {
                    "Failed to read error body"
                }
                Log.w(TAG, "<-- $responseCode ERROR ($duration ms) GET $fullUrl\nError Response: $errBody")
                null
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Log.e(TAG, "<-- FAILED ($duration ms) GET $fullUrl: ${e.message}", e)
            null
        }
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        queryParams: Map<String, String?> = emptyMap(),
        bodyJson: String? = null
    ): T? = withContext(Dispatchers.IO) {
        val fullUrl = buildUrlWithParams(endpoint, queryParams)
        val startTime = System.currentTimeMillis()
        Log.i(TAG, "--> POST $fullUrl\nBody: ${bodyJson ?: "(empty)"}")
        try {
            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "POST"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json; utf-8")
                setRequestProperty("User-Agent", "ThemeAppAndroid/${BuildConfig.VERSION_NAME}")
                doOutput = true
            }

            if (!bodyJson.isNullOrEmpty()) {
                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(bodyJson)
                    writer.flush()
                }
            }

            val responseCode = connection.responseCode
            val duration = System.currentTimeMillis() - startTime
            if (responseCode in 200..299) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                Log.i(TAG, "<-- 200 OK ($duration ms) POST $fullUrl\nResponse: $jsonStr")
                try {
                    val parsed = json.decodeFromString<T>(jsonStr)
                    return@withContext parsed
                } catch (parseEx: Exception) {
                    Log.e(TAG, "JSON Parse Error for POST $fullUrl: ${parseEx.message}\nRaw JSON: $jsonStr", parseEx)
                    null
                }
            } else {
                val errBody = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                } catch (e: Exception) {
                    "Failed to read error body"
                }
                Log.w(TAG, "<-- $responseCode ERROR ($duration ms) POST $fullUrl\nError Response: $errBody")
                null
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Log.e(TAG, "<-- FAILED ($duration ms) POST $fullUrl: ${e.message}", e)
            null
        }
    }
}
