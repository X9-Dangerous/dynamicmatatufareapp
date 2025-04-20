package com.example.dynamic_fare.auth

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

class UserRepository(private val context: Context) {
    private val sqliteUserRepository = SqliteUserRepository(context)

    suspend fun fetchUserRole(email: String): String? = withContext(Dispatchers.IO) {
        try {
            val user = sqliteUserRepository.getUserByEmail(email)
            if (user != null) {
                Log.d("UserRepository", "Fetched role: ${user.role} for email: $email")
                user.role
            } else {
                Log.e("UserRepository", "User not found for email: $email")
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
            // Removed Firebase logic
            Result.failure(Exception("Not implemented"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user data: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun fetchOperatorData(userId: String): Result<OperatorData> {
        return try {
            Log.d("UserRepository", "Attempting to fetch operator data for userId: $userId")
            // Removed Firebase logic
            Result.failure(Exception("Not implemented"))
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching operator data: ${e.message}")
            Result.failure(e)
        }
    }
}