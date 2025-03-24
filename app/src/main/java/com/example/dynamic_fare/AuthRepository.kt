package com.example.dynamic_fare.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun loginWithEmail(email: String, password: String): AuthResult? {
        return try {
            Log.d("AuthRepository", "Attempting login with email: $email") // Log attempt
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "Login successful for user: ${authResult.user?.uid}") // Log success
            authResult
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed: ${e.message}") // Log error message
            Toast.makeText(context, e.message ?: "Login Failed!", Toast.LENGTH_SHORT).show()
            null
        }
    }


    fun getCurrentUserId(): String? {
        val uid = auth.currentUser?.uid
        Log.d("AuthRepository", "Current user ID: $uid")
        return uid
    }

    fun signOut() {
        auth.signOut()
        Log.d("AuthRepository", "User signed out")
    }

    fun googleSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("993474475969-3a9kscepn460n60m27qfbckfg37jr1i2.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): AuthResult? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            Log.d("AuthRepository", "Google sign-in successful for user: ${authResult.user?.uid}")
            authResult
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google Auth failed: ${e.message}")
            null
        }
    }
}
