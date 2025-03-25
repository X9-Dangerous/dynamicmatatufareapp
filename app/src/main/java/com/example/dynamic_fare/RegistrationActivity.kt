package com.example.dynamic_fare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")

        databaseRef.child(userId ?: "").child("operatorId").get()
            .addOnSuccessListener { snapshot ->
                val operatorId = snapshot.getValue(String::class.java) ?: ""
                launchRegistrationScreen(operatorId)
            }
            .addOnFailureListener {
                launchRegistrationScreen("")
            }
    }

    private fun launchRegistrationScreen(operatorId: String) {
        setContent {
            val navController = rememberNavController()
            RegistrationScreen(navController, operatorId)
        }
    }
}
