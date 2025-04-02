package com.example.dynamic_fare.data

import android.util.Log
import com.example.dynamic_fare.models.Fleet
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FleetRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("fleets")

    // ✅ Register a new fleet with coroutines
    suspend fun registerFleet(
        fleetName: String,
        mpesaNumber: String,
        numberOfCars: Int,
        routeStart: String,
        routeEnd: String,
        stops: List<String>,
        operatorId: String
    ): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val fleetId = database.push().key ?: return@withContext null

            val fleetData = mapOf(
                "fleetId" to fleetId,
                "fleetName" to fleetName,
                "mpesaNumber" to mpesaNumber,
                "numberOfCars" to numberOfCars,
                "routeStart" to routeStart,
                "routeEnd" to routeEnd,
                "stops" to stops,
                "operatorId" to operatorId
            )

            database.child(fleetId).setValue(fleetData).await()
            fleetId
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error registering fleet", e)
            null
        }
    }

    // ✅ Fetch fleets for a given operator with real-time updates
    fun fetchFleetsForOperator(operatorId: String, onResult: (List<Fleet>) -> Unit) {
        database.orderByChild("operatorId").equalTo(operatorId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fleets = snapshot.children.mapNotNull { it.getValue(Fleet::class.java) }
                    onResult(fleets)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FleetRepository", "Error fetching fleets", error.toException())
                    onResult(emptyList())
                }
            })
    }

    // ✅ Add a new fleet
    suspend fun addFleet(operatorId: String, fleetName: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val fleetId = database.push().key ?: return@withContext false
            val fleetData = mapOf(
                "fleetId" to fleetId,
                "fleetName" to fleetName,
                "operatorId" to operatorId
            )
            database.child(fleetId).setValue(fleetData).await()
            true
        } catch (e: Exception) {
            Log.e("FleetRepository", "Error adding fleet", e)
            false
        }
    }

    // ✅ Fetch details of a specific fleet with real-time updates
    fun fetchFleetDetails(fleetId: String, onResult: (Fleet?) -> Unit) {
        database.child(fleetId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fleet = snapshot.getValue(Fleet::class.java)
                onResult(fleet)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FleetRepository", "Error fetching fleet details", error.toException())
                onResult(null)
            }
        })
    }

    // ✅ Delete a fleet
    suspend fun deleteFleet(fleetId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val snapshot = database.child(fleetId).get().await()
            if (snapshot.exists()) {
                database.child(fleetId).removeValue().await()
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
}
