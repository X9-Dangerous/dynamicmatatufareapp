package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class SetFaresActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetFaresScreen()
        }
    }
}

@Composable
fun SetFaresScreen() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // State variables to store fare values
    var peakHours by remember { mutableStateOf("") }
    var nonPeakHours by remember { mutableStateOf("") }
    var rainingFare by remember { mutableStateOf("") }
    var nonRainingFare by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues()) // Ensures it doesn't overlap the status bar
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set Fares",
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Fields for fare settings
        OutlinedTextField(
            value = peakHours,
            onValueChange = { peakHours = it },
            label = { Text("Peak Hours Fare") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nonPeakHours,
            onValueChange = { nonPeakHours = it },
            label = { Text("Non-Peak Hours Fare") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = rainingFare,
            onValueChange = { rainingFare = it },
            label = { Text("Fare When Raining") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = nonRainingFare,
            onValueChange = { nonRainingFare = it },
            label = { Text("Fare When Not Raining") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(70.dp))

        // Save Button
        Button(
            onClick = {
                saveFaresToFirebase(db, peakHours, nonPeakHours, rainingFare, nonRainingFare) {
                    message = it
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B51E0)), // Purple color
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Fares")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display message after saving fares
        if (message.isNotEmpty()) {
            Text(text = message, color = Color.Green, fontSize = 16.sp)
        }
    }
}

fun saveFaresToFirebase(
    db: FirebaseFirestore,
    peak: String,
    nonPeak: String,
    rain: String,
    noRain: String,
    onResult: (String) -> Unit
) {
    val fareData = hashMapOf(
        "peak_hours" to peak,
        "non_peak_hours" to nonPeak,
        "raining_fare" to rain,
        "non_raining_fare" to noRain
    )

    db.collection("fares")
        .document("matatu_fares")
        .set(fareData)
        .addOnSuccessListener {
            Log.d("Firestore", "Fares saved successfully")
            onResult("Fares saved successfully!")
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error saving fares", e)
            onResult("Error saving fares: ${e.message}")
        }
}
@Preview(showBackground = true)
@Composable
fun SetFaresScreenPreview() {
    SetFaresScreen()
}
