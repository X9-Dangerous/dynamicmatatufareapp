package com.example.dynamic_fare.data

import android.content.Context
import android.util.Log
import com.example.dynamic_fare.api.BackendApiService
import com.example.dynamic_fare.models.Fleet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FleetRepository(context: Context) {
    private val backendApi = Retrofit.Builder()
        .baseUrl("http://41.89.64.31:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BackendApiService::class.java)

    // Register a new fleet with fleetName and operatorId
    suspend fun registerFleet(
        fleetName: String,
        operatorId: String
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val fleetId = System.currentTimeMillis().toString()
            val fleet = Fleet(
                fleetId = fleetId,
                fleetName = fleetName,
                operatorId = operatorId
            )
            Log.d("FleetRepository", "Sending fleet to backend: $fleet")
            val response = backendApi.createFleet(fleet).execute()
            if (response.isSuccessful) fleetId else null
        } catch (e: Exception) {
            null
        }
    }

    // Fetch fleets for a given operator
    suspend fun fetchFleetsForOperator(operatorId: String): List<Fleet> = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getFleetsForOperator(operatorId).execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error fetching fleets", e)
            emptyList()
        }
    }

    // Get fleets for an operator as a Flow for reactivity
    fun getFleetsForOperatorAsFlow(operatorId: String): Flow<List<Fleet>> = flow {
        try {
            val response = backendApi.getFleetsForOperator(operatorId).execute()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error fetching fleets as flow", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // Fetch details of a specific fleet
    suspend fun fetchFleetDetails(fleetId: String): Fleet? = withContext(Dispatchers.IO) {
        try {
            val response = backendApi.getFleetById(fleetId).execute()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) { null }
    }

    // Delete a fleet
    suspend fun deleteFleet(fleetId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = backendApi.deleteFleet(fleetId).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error deleting fleet", e)
            false
        }
    }

    // Compatibility method for callback-based code that previously used Firebase
    // This simulates the Firebase callback pattern with the new SQLite implementation
    fun fetchFleetDetails(fleetId: String, onResult: (Fleet?) -> Unit) {
        Thread {
            try {
                val response = backendApi.getFleetById(fleetId).execute()
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("FleetRepository", "Error in callback fetchFleetDetails", e)
                onResult(null)
            }
        }.start()
    }
}
