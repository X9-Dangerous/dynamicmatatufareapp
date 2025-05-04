package com.example.dynamic_fare

import com.example.dynamic_fare.api.RetrofitInstance
import okhttp3.*

class FareProcessor(

    private val weatherManager: WeatherManager,
    private val fareManager: FareManager,
    private val timeManager: TimeManager
) {

    fun processQrScan(
        qrCodeData: String,
        city: String,
        isDisabled: Boolean,
        trafficDelay: Boolean,
        updateUi: (Double, String) -> Unit
    ) {
        val matatuId = qrCodeData.trim()  // QR code data should be Matatu ID here

        // Fetch weather information
        weatherManager.fetchWeather { isRaining ->

            // Fetch fare details based on matatuId
            fareManager.fetchFares(matatuId.toIntOrNull() ?: -1) { fares ->

                if (fares != null) {
                    // Determine if it's peak hours
                    val isPeakHours = timeManager.isPeakHours()

                    // Get the final fare and breakdown
                    val (finalFare, breakdown) = fareManager.getFare(fares, isPeakHours, isRaining, isDisabled, trafficDelay)

                    // Update the UI with the calculated fare and breakdown
                    updateUi(finalFare, breakdown)
                } else {
                    updateUi(0.0, "⚠️ No fare data found for Matatu: $matatuId")
                }
            }
        }
    }
}
