package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.dynamic_fare.ui.screens.RegistrationScreen
import com.example.dynamic_fare.utils.FirebaseHelper
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val operatorId = intent.getStringExtra("operatorId")
        Log.d("RegistrationActivity", "Received operatorId from intent: $operatorId")

        if (operatorId != null) {
            Log.d("RegistrationActivity", "Using operatorId from intent: $operatorId")
            launchRegistrationScreen(operatorId)
        } else {
            Log.d("RegistrationActivity", "No operatorId in intent, fetching from Firebase")
            FirebaseHelper.getOperatorId(
                onSuccess = { id -> 
                    Log.d("RegistrationActivity", "Successfully retrieved operatorId from Firebase: $id")
                    launchRegistrationScreen(id)
                },
                onFailure = { 
                    Log.e("RegistrationActivity", "Failed to get operatorId from Firebase")
                    launchRegistrationScreen("") 
                }
            )
        }
    }

    private fun launchRegistrationScreen(operatorId: String) {
        Log.d("RegistrationActivity", "Launching registration screen with operatorId: $operatorId")
        if (operatorId.isBlank()) {
            Log.w("RegistrationActivity", "Warning: Launching with blank operatorId")
        }
        
        setContent {
            val navController = rememberNavController()
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    surface = Color.White,
                    onSurface = Color.Black,
                    primary = Color.Black,
                    onPrimary = Color.White
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RegistrationScreen(
                        navController = navController,
                        operatorId = operatorId
                    )
                }
            }
        }
    }
}
