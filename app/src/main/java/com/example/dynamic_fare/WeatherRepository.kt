package com.example.dynamic_fare.data


import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class WeatherRepository(private val apiKey: String) {

    private val client = OkHttpClient()

    fun getWeather(city: String, callback: (Boolean) -> Unit) {
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Weather API Error: ${e.message}")
                callback(false) // Assume no rain if API fails
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = JSONObject(responseBody.string())
                    val weather = json.getJSONArray("weather").getJSONObject(0).getString("main")
                    val isRaining = weather.contains("Rain", ignoreCase = true)
                    callback(isRaining)
                } ?: callback(false)
            }
        })
    }
}
