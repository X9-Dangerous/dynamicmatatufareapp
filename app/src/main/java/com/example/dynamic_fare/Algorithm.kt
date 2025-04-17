package com.example.dynamic_fare

import android.util.Log
import com.example.dynamic_fare.data.WeatherRepository
import com.google.firebase.database.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import kotlin.math.*
import com.example.dynamic_fare.data.GTFSRepository
import com.example.dynamic_fare.models.MatatuFares

class FareManager(private val database: FirebaseDatabase) {
    fun fetchFares(matatuId: String, callback: (MatatuFares?) -> Unit) {
        val ref = database.getReference("fares/$matatuId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fares = snapshot.getValue(MatatuFares::class.java)
                callback(fares)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to fetch fares: ${error.message}")
                callback(null)
            }
        })
    }

    fun getFare(
        fares: MatatuFares?,
        isPeakHours: Boolean,
        isRaining: Boolean,
        isDisabled: Boolean,
        trafficDelay: Boolean
    ): Pair<Double, String> {
        val baseFare = when {
            (isPeakHours || trafficDelay) && isRaining -> fares?.rainyPeakFare ?: 0.0
            isRaining -> fares?.rainyNonPeakFare ?: 0.0
            isPeakHours || trafficDelay -> fares?.peakFare ?: 0.0
            else -> fares?.nonPeakFare ?: 0.0
        }

        var finalFare = baseFare
        val breakdown = StringBuilder("Base Fare: Ksh $baseFare\n")

        if (isDisabled) {
            val discountPercentage = fares?.disabilityDiscount ?: 0.0
            if (discountPercentage > 0) {
                val discountAmount = (finalFare * (discountPercentage / 100.0))
                finalFare -= discountAmount
                breakdown.append("Disability Discount Applied: -${discountPercentage}% (-Ksh $discountAmount)\n")
            }
        }

        val surgeFactor = if (trafficDelay) 1.2 else 1.0
        finalFare *= surgeFactor
        breakdown.append("Surge Pricing Factor: x$surgeFactor\n")

        finalFare = max(finalFare, 0.0)
        breakdown.append("Final Fare: Ksh $finalFare")

        return Pair(finalFare, breakdown.toString())
    }

    fun getMatatuIdFromRegistration(registrationNumber: String, callback: (String?) -> Unit) {
        val ref = database.getReference("matatus")

        // Query matatus to find the matatuID using the registration number
        ref.orderByChild("registrationNumber").equalTo(registrationNumber).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val matatu = snapshot.children.first().getValue(Matatu::class.java)
                        callback(matatu?.matatuId ?: null)  // Ensure matatuId isn't null
                    } else {
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching matatuID: ${error.message}")
                    callback(null)
                }
            })
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
    val database = FirebaseDatabase.getInstance()
    val fareManager = FareManager(database)
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
    val matatuId: String = "",
    val registrationNumber: String = "",
    val fleetId: String = ""  // Added fleetId field
)