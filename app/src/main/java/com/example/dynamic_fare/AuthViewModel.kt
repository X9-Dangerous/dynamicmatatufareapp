package com.example.dynamic_fare.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun loginUser(email: String, password: String, onResult: (String?) -> Unit) {
        // Validate Inputs First
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            android.util.Log.e("AuthViewModel", "Login failed: Invalid email format: $email")
            onResult("Invalid email format")
            return
        }

        if (password.isBlank() || password.length < 6) {
            android.util.Log.e("AuthViewModel", "Login failed: Password too short")
            onResult("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            try {
                android.util.Log.d("AuthViewModel", "Attempting login for $email")
                val loginSuccess = authRepository.loginWithEmail(email, password)
                if (loginSuccess) {
                    android.util.Log.d("AuthViewModel", "Login success, fetching role for $email")
                    val role = userRepository.fetchUserRole(email)
                    android.util.Log.d("AuthViewModel", "Fetched role: $role for $email")
                    if (role != null) {
                        onResult(role)
                    } else {
                        android.util.Log.e("AuthViewModel", "User does not exist in database: $email")
                        onResult("User does not exist in database")
                    }
                } else {
                    android.util.Log.e("AuthViewModel", "Login failed: No user found for $email")
                    onResult("Login failed: No user found")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error: ${e.message}")
                onResult("Login error: ${e.message}")
            }
        }
    }

    // Google sign-in is not supported in SQLite-only mode. Stub implementation.
    fun googleSignIn(idToken: String, onResult: (String?) -> Unit) {
        onResult("Google sign-in is disabled in offline mode.")
    }
}
