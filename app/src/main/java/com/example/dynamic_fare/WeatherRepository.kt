package com.example.dynamic_fare.data

import android.util.Log
import com.example.dynamic_fare.config.ApiConfig
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class WeatherRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Rate limiting: Only allow one request per 10 seconds
    private var lastRequestTime = 0L
    private val MIN_REQUEST_INTERVAL = 10000L // 10 seconds in milliseconds

    fun getWeather(city: String, callback: (Boolean) -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRequestTime < MIN_REQUEST_INTERVAL) {
            Log.w("WeatherAPI", "Rate limit exceeded. Please wait before making another request.")
            callback(false) // Use cached or default value
            return
        }

        val url = "${ApiConfig.WEATHER_API_BASE_URL}?q=$city&appid=${ApiConfig.WEATHER_API_KEY}"
        
        val request = Request.Builder()
            .url(url)
            .build()

        lastRequestTime = currentTime

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("WeatherAPI", "API call failed: ${e.message}")
                callback(false) // Assume no rain if API fails
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        response.body?.let { responseBody ->
                            try {
                                val json = JSONObject(responseBody.string())
                                val weather = json.getJSONArray("weather").getJSONObject(0).getString("main")
                                val isRaining = weather.contains("Rain", ignoreCase = true)
                                Log.d("WeatherAPI", "Weather condition: $weather, isRaining: $isRaining")
                                callback(isRaining)
                            } catch (e: Exception) {
                                Log.e("WeatherAPI", "Error parsing response: ${e.message}")
                                callback(false)
                            }
                        } ?: run {
                            Log.e("WeatherAPI", "Empty response body")
                            callback(false)
                        }
                    }
                    429 -> {
                        Log.w("WeatherAPI", "Rate limit exceeded (429). Please wait before retrying.")
                        callback(false) // Use cached or default value
                    }
                    404 -> {
                        Log.e("WeatherAPI", "City not found (404)")
                        callback(false)
                    }
                    else -> {
                        Log.e("WeatherAPI", "Unsuccessful response: ${response.code}")
                        callback(false)
                    }
                }
            }
        })
    }
}
