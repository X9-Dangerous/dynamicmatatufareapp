package com.example.dynamic_fare.data

import com.google.firebase.database.FirebaseDatabase

object MpesaRepository {
    val WHITELISTED_MPESA_IPS = setOf(
        "196.201.214.200",
        "196.201.214.206",
        "196.201.213.114",
        "196.201.214.207",
        "196.201.214.208",
        "196.201.213.44",
        "196.201.212.127",
        "196.201.212.138",
        "196.201.212.129",
        "196.201.212.136",
        "196.201.212.74",
        "196.201.212.69"
    )

    private val database = FirebaseDatabase.getInstance()
    private val mpesaRef = database.getReference("mpesa_details")

    fun getMpesaDetails(matatuRegNumber: String, callback: (String?, String?, String?) -> Unit) {
        mpesaRef.child(matatuRegNumber).get().addOnSuccessListener { snapshot ->
            val type = snapshot.child("type").getValue(String::class.java)
            val number = snapshot.child("number").getValue(String::class.java)
            val account = snapshot.child("account").getValue(String::class.java)  // Paybill account reference

            callback(type, number, account)
        }.addOnFailureListener {
            callback(null, null, null)
        }
    }

}
