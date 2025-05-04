package com.example.dynamic_fare.data

import android.content.Context
import android.util.Log
import com.example.dynamic_fare.api.BackendApiService
import com.example.dynamic_fare.models.MatatuFares
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FareRepository(context: Context) {
    private val backendApi = Retrofit.Builder()
        .baseUrl("http://41.89.64.31:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BackendApiService::class.java)


    suspend fun saveFaresRaw(fareData: Map<String, Any?>): String = withContext(Dispatchers.IO) {
        try {
            val requiredKeys = listOf("matatuId", "peakFare", "nonPeakFare", "rainyPeakFare", "rainyNonPeakFare", "disabilityDiscount")
            val nonNullFareData = fareData
                .filterKeys { it in requiredKeys }
                .mapValues { (k, v) ->
                    when (k) {
                        "matatuId" -> v?.toString() ?: ""
                        else -> (v as? Number)?.toDouble() ?: 0.0
                    }
                } as Map<String, Any>
            val response = backendApi.createFareRaw(nonNullFareData).execute()
            if (response.isSuccessful) {
                "✅ Fares saved successfully!"
            } else {
                "Error saving fares: ${response.message()}"
            }
        } catch (e: Exception) {
            "Error saving fares: ${e.message}"
        }
    }

    suspend fun getFareDetails(matatuId: Int): MatatuFares? = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getFareByMatatuId(matatuId).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFaresForMatatu(matatuId: Int): List<MatatuFares> = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getFaresForMatatu(matatuId).execute()
            Log.d("FareRepository", "Raw response body: ${response.body()}")
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FareRepository", "Error fetching fares for matatu: $matatuId", e)
            emptyList()
        }
    }

    suspend fun deleteFare(matatuId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.deleteFare(matatuId).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("FareRepository", "Error deleting fare for matatu: $matatuId", e)
            false
        }
    }

    suspend fun updateFare(fareId: Int, updatedFare: MatatuFares): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.updateFare(fareId, updatedFare).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("FareRepository", "Error updating fare", e)
            false
        }
    }
}
