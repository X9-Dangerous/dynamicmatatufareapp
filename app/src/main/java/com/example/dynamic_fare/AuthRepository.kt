package com.example.dynamic_fare.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context, private val sqliteUserRepository: SqliteUserRepository) {

    suspend fun loginWithEmail(email: String, password: String): Boolean {
        return try {
            Log.d("AuthRepository", "Attempting login with email: $email")
            val user = sqliteUserRepository.authenticateUser(email, password)
            if (user != null) {
                Log.d("AuthRepository", "Login successful for user: ${user.email}")
                true
            } else {
                Log.e("AuthRepository", "Login failed: Invalid credentials")
                Toast.makeText(context, "Invalid email or password!", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}")
            Toast.makeText(context, e.message ?: "Login Failed!", Toast.LENGTH_SHORT).show()
            false
        }
    }


    fun getCurrentUserId(): String? {
        // TODO: Implement getCurrentUserId using sqliteUserRepository
        return null
    }

    fun signOut() {
        // TODO: Implement signOut using sqliteUserRepository
    }

    // Google sign-in is not supported in SQLite-only mode.
    fun googleSignInRequest(): Any? {
        return null
    }

    // Google sign-in is not supported in SQLite-only mode.
    suspend fun firebaseAuthWithGoogle(idToken: String): Any? {
        return null
    }
}
