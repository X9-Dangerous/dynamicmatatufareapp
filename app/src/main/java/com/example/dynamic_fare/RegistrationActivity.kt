package com.example.dynamic_fare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.dynamic_fare.ui.screens.RegistrationScreen
import com.example.dynamic_fare.utils.FirebaseHelper

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseHelper.getOperatorId(
            onSuccess = { operatorId -> launchRegistrationScreen(operatorId) },
            onFailure = { launchRegistrationScreen("") }
        )
    }

    private fun launchRegistrationScreen(operatorId: String) {
        val sampleRoutes = listOf("Route 1", "Route 2", "Route 3") // Example routes

        setContent {
            val navController = rememberNavController()
            RegistrationScreen(navController = navController, operatorId = operatorId)
        }
    }
}
