package com.example.dynamic_fare

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class SignUpUiState {
    var name by mutableStateOf("")
    var surname by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var selectedRole by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)
}
