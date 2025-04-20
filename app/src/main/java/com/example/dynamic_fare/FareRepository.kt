package com.example.dynamic_fare.data

import android.content.Context
import android.util.Log
import com.example.dynamic_fare.AppDatabase
import com.example.dynamic_fare.models.MatatuFares
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FareRepository(context: Context) {
    private val fareDao = AppDatabase.getDatabase(context).fareDao()

    suspend fun saveFares(
        matatuId: String,
        peak: String,
        nonPeak: String,
        rainyPeak: String,
        rainyNonPeak: String,
        discount: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (matatuId.isEmpty()) {
                Log.e("FareRepository", "Invalid matatuId: empty string")
                return@withContext "Error: Invalid Matatu ID"
            }

            // Validate fare values
            val peakFare = peak.toDoubleOrNull()
            val nonPeakFare = nonPeak.toDoubleOrNull()
            val rainyPeakFare = rainyPeak.toDoubleOrNull()
            val rainyNonPeakFare = rainyNonPeak.toDoubleOrNull()
            val disabilityDiscount = discount.toDoubleOrNull()

            if (peakFare == null || nonPeakFare == null || rainyPeakFare == null || rainyNonPeakFare == null) {
                Log.e("FareRepository", "Invalid fare values")
                return@withContext "Error: Please enter valid fare amounts"
            }

            // Create a MatatuFares object
            val fareData = MatatuFares(
                matatuId = matatuId,
                peakFare = peakFare,
                nonPeakFare = nonPeakFare,
                rainyPeakFare = rainyPeakFare,
                rainyNonPeakFare = rainyNonPeakFare,
                disabilityDiscount = disabilityDiscount ?: 0.0
            )

            Log.d("FareRepository", "Saving fares for matatu: $matatuId with data: $fareData")
            fareDao.insertFare(fareData)
            Log.d("FareRepository", "Fares saved successfully for matatu: $matatuId")
            "✅ Fares saved successfully!"
        } catch (e: Exception) {
            Log.e("FareRepository", "Error saving fares for matatu: $matatuId", e)
            "Error saving fares: ${e.message}"
        }
    }

    suspend fun getFareDetails(matatuId: String): MatatuFares? = withContext(Dispatchers.IO) {
        try {
            fareDao.getFareByMatatuId(matatuId)
        } catch (e: Exception) {
            Log.e("FareRepository", "Error fetching fare details for matatu: $matatuId", e)
            null
        }
    }

    suspend fun deleteFare(matatuId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            fareDao.deleteFareByMatatuId(matatuId)
            true
        } catch (e: Exception) {
            Log.e("FareRepository", "Error deleting fare for matatu: $matatuId", e)
            false
        }
    }
}
