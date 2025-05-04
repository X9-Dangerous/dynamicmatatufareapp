package com.example.dynamic_fare.data

import android.content.Context
import android.util.Log
import com.example.dynamic_fare.api.BackendApiService
import com.example.dynamic_fare.models.Matatu
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.PUT
import retrofit2.http.Path

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MatatuRepository(context: Context) {
    private val backendApi = Retrofit.Builder()
        .baseUrl("http://41.89.64.31:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BackendApiService::class.java)

    suspend fun registerMatatu(
        matatuId: String,
        operatorId: String,
        regNumber: String,
        routeStart: String,
        routeEnd: String,
        stops: List<String>,
        mpesaType: String,
        pochiNumber: String,
        paybillNumber: String,
        accountNumber: String,
        tillNumber: String,
        sendMoneyPhone: String,
        fleetname: String = "",
        fleetId: String? = null // <-- New parameter
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val matatu = Matatu(
                matatuId = matatuId,
                operatorId = operatorId ?: "", // Ensure operatorId is set
                registrationNumber = regNumber,
                routeStart = routeStart,
                routeEnd = routeEnd,
                stops = stops,
                mpesaOption = mpesaType,
                pochiNumber = pochiNumber,
                paybillNumber = paybillNumber,
                accountNumber = accountNumber,
                tillNumber = tillNumber,
                sendMoneyPhone = sendMoneyPhone,
                fleetname = fleetname,
                fleetId = fleetId // <-- Pass fleetId if present
            )
            Log.d("MatatuRegistration", "Attempting to register matatu: $matatu")
            Log.d("MatatuRepository", "Passing operatorId to backend: $operatorId")
            val response = backendApi.createMatatu(matatu).execute()
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("MatatuRegistration", "Registration failed. Code: ${response.code()}, Error: $errorBody")
            } else {
                Log.d("MatatuRegistration", "Registration successful: ${response.body()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MatatuRegistration", "Exception during registration: ${e.message}", e)
            false
        }
    }

    suspend fun getMatatuByRegistration(regNumber: String): Matatu? = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getMatatuByRegistration(regNumber).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllMatatus(): List<Matatu> = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.readMatatus().execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteMatatu(matatu: Matatu): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.deleteMatatu(matatu.matatuId).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isValidKenyanPlate(plate: String): Boolean = withContext(Dispatchers.IO) {
        val regex = "^[Kk][A-Z]{2} [0-9]{3}[A-Z]$".toRegex()
        return@withContext regex.matches(plate)
    }

    suspend fun fetchMatatusForOperator(operatorId: String): List<Matatu> = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getMatatusForOperator(operatorId).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchMatatuDetails(matatuId: Int): Matatu? = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getMatatuById(matatuId).execute()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    suspend fun getMatatuIdByRegistration(registrationNumber: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getMatatuByRegistration(registrationNumber).execute()
            if (response.isSuccessful) {
                response.body()?.matatuId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
