package com.example.dynamic_fare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class SignUpViewModelFactory(
    private val auth: FirebaseAuth,
    private val db: DatabaseReference
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(auth, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
