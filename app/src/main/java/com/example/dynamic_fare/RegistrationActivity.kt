package com.example.dynamic_fare

import android.os.Bundle
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

        FirebaseHelper.getOperatorId(
            onSuccess = { operatorId -> launchRegistrationScreen(operatorId) },
            onFailure = { launchRegistrationScreen("") }
        )
    }

    private fun launchRegistrationScreen(operatorId: String) {
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
