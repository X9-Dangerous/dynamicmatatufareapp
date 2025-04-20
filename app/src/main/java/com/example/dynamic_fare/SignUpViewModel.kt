package com.example.dynamic_fare

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SignUpViewModel(
    private val context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun signUpUser(
        name: String,
        surname: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        selectedRole: String,
        termsAccepted: Boolean,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || selectedRole.isBlank()) {
            _errorMessage.value = "All fields are required."
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match."
            return
        }

        if (!termsAccepted) {
            _errorMessage.value = "You must accept the terms and conditions."
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            val success = AuthManager.signUpUser(
                context,
                name,
                surname,
                phone,
                email,
                password,
                confirmPassword,
                selectedRole,
                termsAccepted
            )
            _isLoading.value = false

            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = "Sign-up failed. Please try again."
            }
        }
    }
}
