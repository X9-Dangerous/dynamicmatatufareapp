package com.example.dynamic_fare.auth

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val database = FirebaseDatabase.getInstance().getReference("users")

    suspend fun fetchUserRole(uid: String): String? {
        return try {
            val snapshot = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("role")
                .get()
                .await()

            if (snapshot.exists()) {
                val role = snapshot.getValue(String::class.java)
                Log.d("UserRepository", "Fetched role: $role for UID: $uid")
                role
            } else {
                Log.e("UserRepository", "User role not found for UID: $uid")
                null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user role: ${e.message}")
            null
        }
    }

}
