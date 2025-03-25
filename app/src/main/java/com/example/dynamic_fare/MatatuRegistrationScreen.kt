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
fun MatatuRegistrationScreen(navController: NavController, fleetId: String? = null) {
    var regNumber by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Matatu Registration", fontSize = 24.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = regNumber,
            onValueChange = {
                if (isValidRegistrationPlate(it)) regNumber = it
            },
            label = { Text("Registration Number", color = Color.Black) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = route,
            onValueChange = { route = it },
            label = { Text("Route", color = Color.Black) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            // Save matatu details in Firebase
        }) {
            Text("Register Matatu", color = Color.Black)
        }
    }
}

// Registration Plate Validation
fun isValidRegistrationPlate(regNumber: String): Boolean {
    val pattern = Regex("^[Kk][A-Z]{2} \\d{3}[A-Z]$")
    return pattern.matches(regNumber)
}
