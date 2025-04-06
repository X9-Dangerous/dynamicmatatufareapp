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
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val userData = UserData(
                    name = userDoc.getString("name") ?: "",
                    email = userDoc.getString("email") ?: "",
                    phoneNumber = userDoc.getString("phoneNumber") ?: "",
                    role = userDoc.getString("role") ?: "",
                    profilePicUrl = userDoc.getString("profilePicUrl")
                )
                Result.success(userData)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user data: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun fetchOperatorData(userId: String): Result<OperatorData> {
        return try {
            val operatorDoc = firestore.collection("operators").document(userId).get().await()
            if (operatorDoc.exists()) {
                val operatorData = OperatorData(
                    businessName = operatorDoc.getString("businessName") ?: "",
                    businessAddress = operatorDoc.getString("businessAddress") ?: "",
                    licenseNumber = operatorDoc.getString("licenseNumber") ?: ""
                )
                Result.success(operatorData)
            } else {
                Result.failure(Exception("Operator data not found"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching operator data: ${e.message}")
            Result.failure(e)
        }
    }
}
