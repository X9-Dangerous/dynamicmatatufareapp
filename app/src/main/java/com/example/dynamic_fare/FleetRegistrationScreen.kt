package com.example.dynamic_fare.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
fun FleetRegistrationScreen(navController: NavController, operatorId: String) {
    var fleetName by remember { mutableStateOf("") }
    var mpesaNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Fleet Registration", fontSize = 24.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = fleetName,
            onValueChange = { fleetName = it },
            label = { Text("Fleet Name", color = Color.Black) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = mpesaNumber,
            onValueChange = { mpesaNumber = it },
            label = { Text("M-Pesa Number", color = Color.Black) },
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            // Save fleet details in Firebase
        }) {
            Text("Register Fleet", color = Color.Black)
        }
    }
}
