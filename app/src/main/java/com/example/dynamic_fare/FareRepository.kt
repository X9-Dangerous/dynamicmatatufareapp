package com.example.dynamic_fare.data

import android.util.Log
import com.example.dynamic_fare.models.MatatuFares
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

object FareRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("fares")
    
    init {
        // Enable disk persistence for offline capabilities
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.w("FareRepository", "Persistence already enabled")
        }
    }

    fun saveFares(
        matatuId: String,
        peak: String,
        nonPeak: String,
        rainyPeak: String,
        rainyNonPeak: String,
        discount: String,
        onResult: (String) -> Unit
    ) {
        if (matatuId.isEmpty()) {
            Log.e("FareRepository", "Invalid matatuId: empty string")
            onResult("Error: Invalid Matatu ID")
            return
        }

        // Validate fare values
        val peakFare = peak.toDoubleOrNull()
        val nonPeakFare = nonPeak.toDoubleOrNull()
        val rainyPeakFare = rainyPeak.toDoubleOrNull()
        val rainyNonPeakFare = rainyNonPeak.toDoubleOrNull()
        val disabilityDiscount = discount.toDoubleOrNull()

        if (peakFare == null || nonPeakFare == null || rainyPeakFare == null || rainyNonPeakFare == null) {
            Log.e("FareRepository", "Invalid fare values")
            onResult("Error: Please enter valid fare amounts")
            return
        }

        // Create a map to match the database structure
        val fareData = mapOf(
            "matatuId" to matatuId,  // Add matatuId to the fare data
            "peakFare" to peakFare,
            "nonPeakFare" to nonPeakFare,
            "rainyPeakFare" to rainyPeakFare,
            "rainyNonPeakFare" to rainyNonPeakFare,
            "disabilityDiscount" to (disabilityDiscount ?: 0.0),
            "lastUpdated" to ServerValue.TIMESTAMP  // Add timestamp
        )

        Log.d("FareRepository", "Saving fares for matatu: $matatuId with data: $fareData")
        database.child(matatuId).setValue(fareData)
            .addOnSuccessListener {
                // Keep a reference in the matatus node as well
                val matatuRef = FirebaseDatabase.getInstance().reference
                    .child("matatus")
                    .child(matatuId)
                    .child("fareId")
                matatuRef.setValue(matatuId)
                    .addOnSuccessListener {
                        Log.d("FareRepository", "Fares saved successfully for matatu: $matatuId")
                        onResult("✅ Fares saved successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FareRepository", "Error saving fares for matatu: $matatuId", e)
                        onResult("Error saving fares: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FareRepository", "Error saving fares for matatu: $matatuId", e)
                onResult("Error saving fares: ${e.message}")
            }
    }


    val faresRef = FirebaseDatabase.getInstance().getReference("fares")

    fun getFareDetails(matatuId: String, callback: (MatatuFares?) -> Unit) {
        faresRef.child(matatuId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val fare = MatatuFares(
                            peakFare = snapshot.child("peakFare").getValue(Double::class.java) ?: 0.0,
                            nonPeakFare = snapshot.child("nonPeakFare").getValue(Double::class.java) ?: 0.0,
                            rainyPeakFare = snapshot.child("rainyPeakFare").getValue(Double::class.java) ?: 0.0,
                            rainyNonPeakFare = snapshot.child("rainyNonPeakFare").getValue(Double::class.java) ?: 0.0,
                            disabilityDiscount = snapshot.child("disabilityDiscount").getValue(Double::class.java) ?: 0.0
                        )
                        callback(fare)
                    } else {
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }
}
