package com.example.dynamic_fare

import android.content.Context
import android.util.Log
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.data.GTFSRepository
import com.example.dynamic_fare.data.MatatuRepository
import com.example.dynamic_fare.data.WeatherRepository
import com.example.dynamic_fare.models.Matatu
import com.example.dynamic_fare.models.MatatuFares
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import kotlin.math.*
import kotlinx.coroutines.launch

class FareManager(private val context: Context) {
    private val fareRepository = FareRepository(context)
    private val matatuRepository = MatatuRepository(context)

    fun fetchFares(matatuId: Int, callback: (List<MatatuFares>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val fares = fareRepository.getFaresForMatatu(matatuId)
            CoroutineScope(Dispatchers.Main).launch {
                callback(fares)
            }
        }
    }

    fun getFare(
        fares: List<MatatuFares>,
        isPeakHours: Boolean,
        isRaining: Boolean,
        isDisabled: Boolean,
        trafficDelay: Boolean
    ): Pair<Double, String> {
        // Pick the correct fare object by fare type (if you have multiple fares for a matatu)
        val fare = when {
            isPeakHours && isRaining -> fares.find { it.rainyPeakFare > 0.0 }
            isPeakHours && !isRaining -> fares.find { it.peakFare > 0.0 }
            !isPeakHours && isRaining -> fares.find { it.rainyNonPeakFare > 0.0 }
            else -> fares.find { it.nonPeakFare > 0.0 }
        }

        val baseFare = when {
            isPeakHours && isRaining -> fare?.rainyPeakFare ?: 0.0
            isPeakHours && !isRaining -> fare?.peakFare ?: 0.0
            !isPeakHours && isRaining -> fare?.rainyNonPeakFare ?: 0.0
            else -> fare?.nonPeakFare ?: 0.0
        }

        var finalFare = baseFare
        val breakdown = StringBuilder("Base Fare: Ksh $baseFare\n")

        if (isDisabled) {
            val discount = fare?.disabilityDiscount ?: 0.0
            if (discount > 0.0) {
                val discountAmount = finalFare * (discount / 100.0)
                finalFare -= discountAmount
                breakdown.append("Disability Discount Applied: -Ksh $discountAmount\n")
            }
        }

        if (trafficDelay) {
            finalFare *= 1.2 // Assuming 20% surge for traffic delay
            breakdown.append("Traffic Delay Surge Applied: x1.2\n")
        }

        finalFare = max(finalFare, 0.0)
        breakdown.append("Final Fare: Ksh $finalFare")

        return Pair(finalFare, breakdown.toString())
    }

    fun getMatatuIdFromRegistration(registrationNumber: String, callback: (Int?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val matatu = matatuRepository.getMatatuByRegistration(registrationNumber)
            CoroutineScope(Dispatchers.Main).launch {
                callback(matatu?.matatuId?.toIntOrNull())
            }
        }
    }
}

class WeatherManager {
    private val weatherRepository: WeatherRepository = WeatherRepository()
    private var cachedWeather: Boolean? = null
    private var lastFetchTime: Long = 0

    fun fetchWeather(callback: (Boolean) -> Unit) {
        val city = "Nairobi" // Always use Nairobi
        val currentTime = System.currentTimeMillis()

        // Return cached result if available and less than 30 minutes old
        if (cachedWeather != null && currentTime - lastFetchTime < 30 * 60 * 1000) {
            android.util.Log.d("WeatherManager", "Using cached weather data: isRaining=$cachedWeather")
            callback(cachedWeather!!)
            return
        }

        weatherRepository.getWeather(city) { isRaining ->
            // Update cache with new weather data
            cachedWeather = isRaining
            lastFetchTime = currentTime

            android.util.Log.d("WeatherManager", "Updated weather data: isRaining=$isRaining")
            callback(isRaining)
        }
    }
}

class TimeManager {
    fun isPeakHours(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 6..9 || hour in 16..20
    }
}

// Main function to process QR scan
fun processQrScan(
    qrCodeData: String,
    city: String,
    isDisabled: Boolean,
    trafficDelay: Boolean,
    gtfsRepository: GTFSRepository,
    updateUi: (Double, String) -> Unit
) {
    val fareManager = FareManager(context = gtfsRepository.context)
    val weatherManager = WeatherManager()
    val timeManager = TimeManager()

    val matatuRegNo = qrCodeData.trim()

    // Step 1: Fetch matatuID using matatuRegNo
    fareManager.getMatatuIdFromRegistration(registrationNumber = matatuRegNo) { matatuId ->
        if (matatuId != null) {
            // Step 2: Fetch fares using matatuID
            weatherManager.fetchWeather { isRaining ->
                fareManager.fetchFares(matatuId) { fares ->
                    if (fares != null) {
                        val isPeakHours = timeManager.isPeakHours()
                        val (finalFare, breakdown) = fareManager.getFare(fares, isPeakHours, isRaining, isDisabled, trafficDelay)
                        updateUi(finalFare, breakdown)
                    } else {
                        Log.e("FareManager", "No fare data found for Matatu ID: $matatuId")
                        updateUi(0.0, "⚠️ No fare data found for Matatu ID: $matatuId")
                    }
                }
            }
        } else {
            Log.e("FareManager", "No matatuID found for Matatu Registration Number: $matatuRegNo")
            updateUi(0.0, "⚠️ No Matatu found with Registration Number: $matatuRegNo")
        }
    }
}

// Assuming Matatu class has fields `matatuID` and `registrationNumber`
data class Matatu(
    val matatuId: Int = 0,
    val registrationNumber: String = "",
    val fleetId: String = ""  // Added fleetId field
)