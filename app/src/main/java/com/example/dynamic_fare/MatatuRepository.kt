package com.example.dynamic_fare.data

import android.util.Log
import com.example.dynamic_fare.models.Matatu
import com.google.firebase.database.*

object MatatuRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("matatus")

    fun isValidKenyanPlate(plate: String): Boolean {
        val regex = "^[Kk][A-Z]{2} [0-9]{3}[A-Z]$".toRegex()
        return regex.matches(plate)
    }

    // ✅ Register a new matatu (without storing QR Code)
    fun registerMatatu(
        matatuId: String,
        operatorId: String,
        regNumber: String,
        routeStart: String,
        routeEnd: String,
        stops: MutableList<String>,
        mpesaType: String,
        pochiNumber: String,
        paybillNumber: String,
        accountNumber: String,
        tillNumber: String,
        sendMoneyPhone: String,
        onComplete: (Boolean) -> Unit
    ) {
        // Check if registration number already exists
        database.orderByChild("registrationNumber").equalTo(regNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.e("MatatuRepository", "Registration number already exists!")
                        onComplete(false)
                    } else {
                        val matatuId = database.push().key ?: return // Generate unique ID

                        val matatuData = mapOf(
                            "operatorId" to operatorId,
                            "registrationNumber" to regNumber,
                            "routeStart" to routeStart,
                            "routeEnd" to routeEnd,
                            "stops" to stops,
                            "mpesaOption" to mpesaType,
                            "pochiNumber" to pochiNumber,
                            "paybillNumber" to paybillNumber,
                            "accountNumber" to accountNumber,
                            "tillNumber" to tillNumber,
                            "sendMoneyPhone" to sendMoneyPhone
                        )

                        database.child(matatuId).setValue(matatuData)
                            .addOnSuccessListener {
                                onComplete(true)  // Registration successful
                            }
                            .addOnFailureListener {
                                Log.e("MatatuRepository", "Error registering matatu", it)
                                onComplete(false)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MatatuRepository", "Error checking registration number: \${error.message}")
                    onComplete(false)
                }
            })
    }

    // ✅ Fetch all matatus under a specific operator (Real-Time Updates)
    fun fetchMatatusForOperator(operatorId: String, onResult: (List<Matatu>) -> Unit) {
        database.orderByChild("operatorId").equalTo(operatorId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val matatus = snapshot.children.mapNotNull { it.getValue(Matatu::class.java) }
                    onResult(matatus)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MatatuRepository", "Error fetching matatus: \${error.message}")
                    onResult(emptyList())
                }
            })
    }

    // ✅ Fetch a single matatu's details (Real-Time Updates)
    fun fetchMatatuDetails(matatuId: String, onResult: (Matatu?) -> Unit) {
        database.child(matatuId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val matatu = snapshot.getValue(Matatu::class.java)
                    onResult(matatu)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MatatuRepository", "Error fetching matatu details: \${error.message}")
                    onResult(null)
                }
            })
    }

    // ✅ Delete a matatu and its associated fare details
    fun deleteMatatu(matatuId: String, onComplete: (Boolean) -> Unit) {
        Log.d("MatatuRepository", "Starting deletion process for matatuId: $matatuId")
        
        // Delete matatu details
        database.child(matatuId).removeValue()
            .addOnSuccessListener {
                Log.d("MatatuRepository", "Successfully deleted matatu: $matatuId")
                
                // Also delete associated fare details
                val faresRef = FirebaseDatabase.getInstance().reference.child("fares")
                faresRef.child(matatuId).removeValue()
                    .addOnSuccessListener {
                        Log.d("MatatuRepository", "Successfully deleted fare details for matatu: $matatuId")
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("MatatuRepository", "Error deleting fare details for matatu: $matatuId", e)
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MatatuRepository", "Error deleting matatu: $matatuId", e)
                onComplete(false)
            }
    }

    // ✅ Get matatuId by registration number
    fun getMatatuIdByRegistration(registrationNumber: String, onResult: (String?) -> Unit) {
        database.orderByChild("registrationNumber").equalTo(registrationNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the first matching matatu's ID
                        val matatuId = snapshot.children.first().key
                        Log.d("MatatuRepository", "Found matatuId: $matatuId for registration: $registrationNumber")
                        onResult(matatuId)
                    } else {
                        Log.e("MatatuRepository", "No matatu found for registration: $registrationNumber")
                        onResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MatatuRepository", "Error finding matatu by registration: ${error.message}")
                    onResult(null)
                }
            })
    }
}
