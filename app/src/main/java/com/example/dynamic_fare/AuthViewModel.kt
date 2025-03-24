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
            onResult("Invalid email format")
            return
        }

        if (password.isBlank() || password.length < 6) {
            onResult("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            try {
                val authResult = authRepository.loginWithEmail(email, password)
                val user = authResult?.user

                if (user != null) {
                    val uid = user.uid
                    Log.d("AuthViewModel", "Login successful! UID: $uid")

                    val role = userRepository.fetchUserRole(uid)
                    if (role != null) {
                        onResult(role)
                    } else {
                        onResult("User does not exist in database")
                    }
                } else {
                    Log.e("AuthViewModel", "Login failed: User is null")
                    onResult("Login failed: No user found")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error: ${e.message}")
                onResult("Login error: ${e.message}")
            }
        }
    }

    fun googleSignIn(idToken: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val authResult = authRepository.firebaseAuthWithGoogle(idToken)
                val uid = authResult?.user?.uid

                if (uid != null) {
                    val role = userRepository.fetchUserRole(uid)
                    Log.d("AuthViewModel", "Google sign-in role: $role")
                    onResult(role)
                } else {
                    Log.e("AuthViewModel", "Google sign-in failed: No UID")
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google sign-in error: ${e.message}")
                onResult(null)
            }
        }
    }
}
