package com.example.dynamic_fare.data

import android.util.Log
import com.example.dynamic_fare.models.MatatuFares
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FareRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("fares")

    fun saveFares(
        matatuId: String,
        peak: String,
        nonPeak: String,
        rainyPeak: String,
        rainyNonPeak: String,
        discount: String,
        onResult: (String) -> Unit
    ) {
        val fareData = MatatuFares(
            peakFare = peak.toDoubleOrNull() ?: 0.0,
            nonPeakFare = nonPeak.toDoubleOrNull() ?: 0.0,
            rainyPeakFare = rainyPeak.toDoubleOrNull() ?: 0.0,
            rainyNonPeakFare = rainyNonPeak.toDoubleOrNull() ?: 0.0,
            disabilityDiscount = discount.toDoubleOrNull() ?: 0.0
        )

        database.child(matatuId).setValue(fareData)
            .addOnSuccessListener {
                Log.d("FareRepository", "Fares saved successfully")
                onResult(" Fares saved successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FareRepository", "Error saving fares", e)
                onResult("Error saving fares: ${e.message}")
            }
    }


    private val faresRef = FirebaseDatabase.getInstance().getReference("fares")

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
