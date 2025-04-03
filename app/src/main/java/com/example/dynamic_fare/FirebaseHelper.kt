package com.example.dynamic_fare.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getOperatorId(onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val userId = getCurrentUserId() ?: return onFailure()

        database.getReference("users").child(userId).child("operatorId").get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.getValue(String::class.java) ?: "")
            }
            .addOnFailureListener {
                onFailure()
            }
    }
}
