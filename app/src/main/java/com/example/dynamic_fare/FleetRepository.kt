package com.example.dynamic_fare.data

import android.content.Context
import android.util.Log
import com.example.dynamic_fare.AppDatabase
import com.example.dynamic_fare.models.Fleet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FleetRepository(context: Context) {
    private val fleetDao = AppDatabase.getDatabase(context).fleetDao()

    // Register a new fleet with fleetName and operatorId
    suspend fun registerFleet(
        fleetName: String,
        operatorId: String
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            // Generate a unique ID for the fleet
            val fleetId = System.currentTimeMillis().toString()
            val fleet = Fleet(
                fleetId = fleetId,
                fleetName = fleetName,
                operatorId = operatorId
            )
            fleetDao.insertFleet(fleet)
            fleetId
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error registering fleet", e)
            null
        }
    }

    // Fetch fleets for a given operator
    suspend fun fetchFleetsForOperator(operatorId: String): List<Fleet> = withContext(Dispatchers.IO) {
        try {
            fleetDao.getFleetsByOperatorId(operatorId)
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error fetching fleets", e)
            emptyList()
        }
    }

    // Get fleets for an operator as a Flow for reactivity
    fun getFleetsForOperatorAsFlow(operatorId: String): Flow<List<Fleet>> = flow {
        try {
            val fleets = fleetDao.getFleetsByOperatorId(operatorId)
            emit(fleets)
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error fetching fleets as flow", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    // Fetch details of a specific fleet
    suspend fun fetchFleetDetails(fleetId: String): Fleet? = withContext(Dispatchers.IO) {
        try {
            fleetDao.getFleetById(fleetId)
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error fetching fleet details", e)
            null
        }
    }

    // Delete a fleet
    suspend fun deleteFleet(fleetId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val fleet = fleetDao.getFleetById(fleetId)
            if (fleet != null) {
                fleetDao.deleteFleetById(fleetId)
                true
            } else {
                Log.e("FleetRepository", "Fleet not found")
                false
            }
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
                // Use runBlocking to call the suspend function from a non-coroutine context
                // This is safe because we're already in a background thread
                val fleet = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) { 
                    fleetDao.getFleetById(fleetId) 
                }
                onResult(fleet)
            } catch (e: Exception) {
                Log.e("FleetRepository", "Error in callback fetchFleetDetails", e)
                onResult(null)
            }
        }.start()
    }
}
