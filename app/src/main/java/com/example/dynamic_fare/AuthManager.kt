package com.example.dynamic_fare

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AuthManager {
    suspend fun signUpUser(
        auth: FirebaseAuth,
        db: DatabaseReference,
        name: String,
        surname: String,
        phone: String,
        email: String,
        password: String,
        confirmPassword: String,
        selectedRole: String,
        termsAccepted: Boolean
    ): Boolean {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || selectedRole.isBlank()) {
            return false
        }

        if (password != confirmPassword) {
            return false
        }

        if (!termsAccepted) {
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                user?.let {
                    val userData = mapOf(
                        "name" to name,
                        "surname" to surname,
                        "phone" to phone,
                        "email" to email,
                        "role" to selectedRole
                    )
                    db.child("users").child(it.uid).setValue(userData).await()
                }
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
