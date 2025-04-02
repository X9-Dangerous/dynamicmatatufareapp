package com.example.dynamic_fare.data

import com.google.firebase.database.FirebaseDatabase

object MpesaRepository {
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
