package com.example.dynamic_fare.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MatatuRegistrationScreen(navController: NavController, operatorId: String, fleetId: String? = null) {
    var regNumber by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Matatu Registration", fontSize = 24.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = regNumber,
            onValueChange = {
                if (isValidRegistrationPlate(it)) {
                    regNumber = it
                    errorMessage = null
                } else {
                    errorMessage = "Invalid registration number format."
                }
            },
            label = { Text("Registration Number") },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = route,
            onValueChange = { route = it },
            label = { Text("Route") },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            if (regNumber.isNotEmpty() && route.isNotEmpty()) {
                saveMatatuDetails(operatorId, regNumber, route, fleetId) { success, message ->
                    if (success) {
                        navController.popBackStack()
                    } else {
                        errorMessage = message
                    }
                }
            } else {
                errorMessage = "Please fill in all fields."
            }
        }) {
            Text("Register Matatu", color = Color.White)
        }
    }
}

// Registration Plate Validation
fun isValidRegistrationPlate(regNumber: String): Boolean {
    val pattern = Regex("^[Kk][A-Z]{2} \\d{3}[A-Z]$")
    return pattern.matches(regNumber)
}

// Save Matatu Details (Replace with actual Firebase logic)
fun saveMatatuDetails(operatorId: String, regNumber: String, route: String, fleetId: String?, onComplete: (Boolean, String?) -> Unit) {
    // Simulated Firebase function (Replace with actual Firebase DB code)
    if (regNumber.startsWith("K")) {
        onComplete(true, null) // Success
    } else {
        onComplete(false, "Failed to save matatu details.") // Simulated failure
    }
}
