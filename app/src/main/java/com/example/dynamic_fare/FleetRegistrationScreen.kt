package com.example.dynamic_fare.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.data.FleetRepository
import kotlinx.coroutines.launch

@Composable
fun FleetRegistrationScreen(navController: NavController, operatorId: String) {
    var fleetName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val fleetRepository = remember(context) { FleetRepository(context) }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Fleet Registration", fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))
        TextField(
            value = fleetName,
            onValueChange = { fleetName = it },
            label = { Text("Fleet Name") },
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            if (fleetName.isBlank()) {
                errorMessage = "Please enter a fleet name."
            } else {
                coroutineScope.launch {
                    val fleetId = fleetRepository.registerFleet(
                        fleetName = fleetName,
                        operatorId = operatorId
                    )
                    if (fleetId != null) {
                        navController.popBackStack()
                    } else {
                        errorMessage = "Failed to register fleet."
                    }
                }
            }
        }) {
            Text("Register Fleet")
        }
    }
}
