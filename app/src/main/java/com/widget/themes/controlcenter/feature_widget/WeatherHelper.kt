package com.widget.themes.controlcenter.feature_widget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.widget.themes.controlcenter.core.utils.SharedPreferencesUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

object WeatherHelper {

    data class WeatherData(
        val temp: String,
        val condition: String,
        val city: String,
        val range: String
    )

    fun getWeatherData(context: Context): WeatherData {
        val prefs = SharedPreferencesUtils.getPrefs(context, SharedPreferencesUtils.PREFS_WIDGET)
        val temp = prefs.getString("weather_temp", null)
        val condition = prefs.getString("weather_condition", null)
        val city = prefs.getString("weather_city", null)
        val range = prefs.getString("weather_range", null)

        if (temp != null && condition != null && city != null && range != null) {
            return WeatherData(temp, condition, city, range)
        }

        // Default initial fallback
        return WeatherData(
            temp = "29°",
            condition = "Partly Cloudy",
            city = getDefaultCityName(),
            range = "25° / 33°"
        )
    }

    fun updateWeather(context: Context, onComplete: ((WeatherData) -> Unit)? = null) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (hasFine || hasCoarse) {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)
                    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    fetchWeatherFromCoordinates(appContext, location.latitude, location.longitude, onComplete)
                                }
                            } else {
                                fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                                    if (lastLoc != null) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            fetchWeatherFromCoordinates(appContext, lastLoc.latitude, lastLoc.longitude, onComplete)
                                        }
                                    } else {
                                        fetchDefaultWeather(appContext, onComplete)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener {
                            fetchDefaultWeather(appContext, onComplete)
                        }
                } else {
                    fetchDefaultWeather(appContext, onComplete)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchDefaultWeather(context: Context, onComplete: ((WeatherData) -> Unit)?) {
        CoroutineScope(Dispatchers.IO).launch {
            // Default coordinates for Hanoi (21.0285, 105.8542) or user's locale
            val defaultLat = 21.0285
            val defaultLon = 105.8542
            fetchWeatherFromCoordinates(context, defaultLat, defaultLon, onComplete)
        }
    }

    private suspend fun fetchWeatherFromCoordinates(
        context: Context,
        lat: Double,
        lon: Double,
        onComplete: ((WeatherData) -> Unit)?
    ) {
        try {
            var cityName = ""
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                cityName = addresses?.firstOrNull()?.locality
                    ?: addresses?.firstOrNull()?.subAdminArea
                    ?: addresses?.firstOrNull()?.adminArea
                    ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (cityName.isBlank()) {
                cityName = getDefaultCityName()
            }

            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val json = JSONObject(sb.toString())
                val current = json.getJSONObject("current")
                val currentTemp = current.getDouble("temperature_2m").roundToInt()
                val weatherCode = current.getInt("weather_code")
                val condition = mapWeatherCode(weatherCode)

                val daily = json.getJSONObject("daily")
                val minTemp = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt()
                val maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt()

                val weatherData = WeatherData(
                    temp = "${currentTemp}°",
                    condition = condition,
                    city = cityName,
                    range = "${minTemp}° / ${maxTemp}°"
                )

                // Save to SharedPreferences
                SharedPreferencesUtils.getPrefs(context, SharedPreferencesUtils.PREFS_WIDGET).edit()
                    .putString("weather_temp", weatherData.temp)
                    .putString("weather_condition", weatherData.condition)
                    .putString("weather_city", weatherData.city)
                    .putString("weather_range", weatherData.range)
                    .putLong("weather_last_updated", System.currentTimeMillis())
                    .apply()

                withContext(Dispatchers.Main) {
                    onComplete?.invoke(weatherData)
                    // Trigger widgets to update
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    val widget2x2Ids = appWidgetManager.getAppWidgetIds(
                        android.content.ComponentName(context, Widget2x2Provider::class.java)
                    )
                    for (id in widget2x2Ids) {
                        Widget2x2Provider().updateWidget(context, appWidgetManager, id)
                    }
                    val widget4x2Ids = appWidgetManager.getAppWidgetIds(
                        android.content.ComponentName(context, Widget4x2Provider::class.java)
                    )
                    for (id in widget4x2Ids) {
                        Widget4x2Provider().updateWidget(context, appWidgetManager, id)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDefaultCityName(): String {
        return try {
            val country = Locale.getDefault().country
            if (country.equals("VN", ignoreCase = true)) "Hanoi" else "My City"
        } catch (e: Exception) {
            "Washington"
        }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63 -> "Rain"
            65 -> "Heavy Rain"
            66, 67 -> "Freezing Rain"
            71, 73 -> "Snow"
            75, 77 -> "Heavy Snow"
            80, 81 -> "Showers"
            82 -> "Violent Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Partly Cloudy"
        }
    }
}
