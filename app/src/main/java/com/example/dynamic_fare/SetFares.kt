package com.example.dynamic_fare


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.ui.screens.FareTabbedActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import androidx.compose.material3.ButtonDefaults


class SetFaresActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val matatuId = intent.getStringExtra("matatuId") ?: ""
        Log.d("SetFaresActivity", "Received matatuId: $matatuId")
        setContent {
            SetFaresScreen(matatuId)
        }
    }
}

@Composable
fun SetFaresScreen(matatuId: String) {
    Log.d("SetFaresScreen", "Rendering with matatuId: $matatuId")
    val db = FirebaseDatabase.getInstance()
    val context = LocalContext.current

    // State variables to store fare values
    var peakHours by remember { mutableStateOf("") }
    var nonPeakHours by remember { mutableStateOf("") }
    var rainingPeakFare by remember { mutableStateOf("") }
    var rainingNonPeakFare by remember { mutableStateOf("") }
    var disabilityDiscount by remember { mutableStateOf("") }
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
            label = { Text("Set Non-Peak Hours Fare") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = rainingPeakFare,
            onValueChange = { rainingPeakFare = it },
            label = { Text(" Set Rainy Peak Hours Fare") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = rainingNonPeakFare,
            onValueChange = { rainingNonPeakFare = it },
            label = { Text("Set Rainy Non-Peak Hours Fare") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = disabilityDiscount,
            onValueChange = { disabilityDiscount = it },
            label = { Text("Set Disability Discount % (Optional)") },
            placeholder = { Text("Enter percentage e.g. 20 for 20%") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(70.dp))

        // Save Button

        Button(
            onClick = {
                // Validation block
                if (matatuId.isEmpty()) {
                    message = "Error: Invalid Matatu ID"
                    return@Button
                }
                Log.d("SetFaresScreen", "Saving fares for matatuId: $matatuId")
                Log.d("SetFaresScreen", "Fare values: peak=$peakHours, nonPeak=$nonPeakHours, rainyPeak=$rainingPeakFare, rainyNonPeak=$rainingNonPeakFare, discount=$disabilityDiscount")
                
                FareRepository.saveFares(
                    matatuId = matatuId,
                    peak = peakHours,
                    nonPeak = nonPeakHours,
                    rainyPeak = rainingPeakFare,
                    rainyNonPeak = rainingNonPeakFare,
                    discount = disabilityDiscount
                ) { resultMessage ->
                    message = resultMessage

                    if (resultMessage == "✅ Fares saved successfully!") {
                        Handler(Looper.getMainLooper()).post {
                            // Navigate back to DetailMatatu
                            val intent = Intent(context, FareTabbedActivity::class.java).apply {
                                putExtra("matatuId", matatuId)
                                // Clear the back stack
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            (context as? ComponentActivity)?.finish() // Close the current activity
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9B51E0)
            )
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



@Preview(showBackground = true)
@Composable
private fun SetFaresScreenPreview() {
    SetFaresScreen(matatuId = "preview_id")
}
