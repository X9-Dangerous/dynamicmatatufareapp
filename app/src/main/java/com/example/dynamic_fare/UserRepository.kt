package com.example.dynamic_fare.auth

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserData(
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: String = "",
    val profilePicUrl: String? = null
)

data class OperatorData(
    val businessName: String = "",
    val businessAddress: String = "",
    val licenseNumber: String = ""
)

class UserRepository {
    private val database = FirebaseDatabase.getInstance().getReference("users")
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun fetchUserRole(uid: String): String? {
        return try {
            val snapshot = database
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

    suspend fun fetchUserData(userId: String): Result<UserData> {
        return try {
            Log.d("UserRepository", "Attempting to fetch user data for userId: $userId")
            val snapshot = database.child(userId).get().await()

            if (snapshot.exists()) {
                val userData = UserData(
                    name = snapshot.child("name").getValue(String::class.java) ?: "",
                    email = snapshot.child("email").getValue(String::class.java) ?: "",
                    phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "",
                    role = snapshot.child("role").getValue(String::class.java) ?: "",
                    profilePicUrl = snapshot.child("profilePicUrl").getValue(String::class.java)
                )
                Log.d("UserRepository", "Successfully fetched user data: $userData")
                Result.success(userData)
            } else {
                Log.e("UserRepository", "User not found for userId: $userId")
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user data: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun fetchOperatorData(userId: String): Result<OperatorData> {
        return try {
            Log.d("UserRepository", "Attempting to fetch operator data for userId: $userId")
            val snapshot = database.child(userId).get().await()

            if (snapshot.exists() && snapshot.child("role").getValue(String::class.java) == "Matatu Operator") {
                val operatorData = OperatorData(
                    businessName = snapshot.child("businessName").getValue(String::class.java) ?: "",
                    businessAddress = snapshot.child("businessAddress").getValue(String::class.java) ?: "",
                    licenseNumber = snapshot.child("licenseNumber").getValue(String::class.java) ?: ""
                )
                Log.d("UserRepository", "Successfully fetched operator data: $operatorData")
                Result.success(operatorData)
            } else {
                Log.e("UserRepository", "Operator data not found for userId: $userId")
                Result.failure(Exception("Operator data not found"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching operator data: ${e.message}")
            Result.failure(e)
        }
    }
}