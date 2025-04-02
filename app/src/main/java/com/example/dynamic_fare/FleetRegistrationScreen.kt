package com.example.dynamic_fare.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FleetRegistrationScreen(navController: NavController, operatorId: String) {
    var currentStep by remember { mutableStateOf(0) }

    var fleetName by remember { mutableStateOf("") }
    var routeStart by remember { mutableStateOf("") }
    var routeEnd by remember { mutableStateOf("") }
    var stops by remember { mutableStateOf(mutableListOf<String>()) }
    var stopText by remember { mutableStateOf("") }

    var mpesaType by remember { mutableStateOf("") } // "Pochi", "Paybill", "Till", "Send Money"
    var mpesaNumber by remember { mutableStateOf("") }
    var paybillAccount by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Fleet Registration", fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(progress = (currentStep + 1) / 3f, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(20.dp))

        when (currentStep) {
            0 -> FleetNameStep(fleetName) { fleetName = it }
            1 -> RouteStep(routeStart, routeEnd, stops, stopText, {
                routeStart = it
            }, {
                routeEnd = it
            }, {
                stopText = it
            }, {
                if (stopText.isNotBlank()) {
                    stops = stops.toMutableList().apply { add(stopText) }
                    stopText = ""
                }
            }, { stop -> stops = stops.toMutableList().apply { remove(stop) } })
            2 -> MpesaDetailsStep(mpesaType, mpesaNumber, paybillAccount,
                onMpesaTypeChange = { mpesaType = it },
                onMpesaNumberChange = { mpesaNumber = it },
                onPaybillAccountChange = { paybillAccount = it }
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                Button(onClick = { currentStep-- }) { Text("Back") }
            }
            Button(onClick = {
                if (validateStep(currentStep, fleetName, routeStart, routeEnd, mpesaType, mpesaNumber)) {
                    if (currentStep < 2) currentStep++
                } else {
                    errorMessage = "Please fill in all required fields."
                }
            }) {
                Text(if (currentStep == 2) "Submit" else "Next")
            }
        }
    }
}

@Composable
fun FleetNameStep(fleetName: String, onFleetNameChange: (String) -> Unit) {
    Column {
        Text("Enter Fleet Name")
        TextField(value = fleetName, onValueChange = onFleetNameChange)
    }
}

@Composable
fun RouteStep(
    routeStart: String,
    routeEnd: String,
    stops: List<String>,
    stopText: String,
    onRouteStartChange: (String) -> Unit,
    onRouteEndChange: (String) -> Unit,
    onStopTextChange: (String) -> Unit,
    onAddStop: () -> Unit,
    onRemoveStop: (String) -> Unit
) {
    Column {
        Text("Enter Route Details")
        TextField(value = routeStart, onValueChange = onRouteStartChange, label = { Text("Start Location") })
        TextField(value = routeEnd, onValueChange = onRouteEndChange, label = { Text("End Location") })

        Text("Stops (Optional):")
        stops.forEach { stop ->
            Row {
                Text(stop)
                Button(onClick = { onRemoveStop(stop) }) { Text("Remove") }
            }
        }
        TextField(value = stopText, onValueChange = onStopTextChange, label = { Text("Add Stop (Optional)") })
        Button(onClick = onAddStop) { Text("Add Stop") }
    }
}

@Composable
fun MpesaDetailsStep(
    mpesaType: String,
    mpesaNumber: String,
    paybillAccount: String,
    onMpesaTypeChange: (String) -> Unit,
    onMpesaNumberChange: (String) -> Unit,
    onPaybillAccountChange: (String) -> Unit
) {
    Column {
        Text("Enter M-Pesa Details")
        TextField(value = mpesaType, onValueChange = onMpesaTypeChange, label = { Text("M-Pesa Type") })
        TextField(value = mpesaNumber, onValueChange = onMpesaNumberChange, label = { Text("M-Pesa Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        TextField(value = paybillAccount, onValueChange = onPaybillAccountChange, label = { Text("Paybill Account (if applicable)") })
    }
}

fun validateStep(step: Int, fleetName: String, routeStart: String, routeEnd: String, mpesaType: String, mpesaNumber: String): Boolean {
    return when (step) {
        0 -> fleetName.isNotBlank()
        1 -> routeStart.isNotBlank() && routeEnd.isNotBlank() // Stops are now optional
        2 -> mpesaType.isNotBlank() && mpesaNumber.isNotBlank()
        else -> true
    }
}
