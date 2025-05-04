package com.example.dynamic_fare

import android.content.Intent
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
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.ui.screens.FareTabbedActivity
import androidx.compose.material3.ButtonDefaults
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fareRepository = remember(context) { FareRepository(context) }

    var peakHours by remember { mutableStateOf("") }
    var nonPeakHours by remember { mutableStateOf("") }
    var rainingPeakFare by remember { mutableStateOf("") }
    var rainingNonPeakFare by remember { mutableStateOf("") }
    var disabilityDiscount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
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
            label = { Text("Set Disability Discount (%)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(70.dp))

        Button(
            onClick = {
                if (matatuId.isEmpty()) {
                    message = "Error: Invalid Matatu ID"
                    return@Button
                }
                val peakFare = peakHours.toDoubleOrNull()
                val nonPeakFare = nonPeakHours.toDoubleOrNull()
                val rainyPeakFareVal = rainingPeakFare.toDoubleOrNull()
                val rainyNonPeakFareVal = rainingNonPeakFare.toDoubleOrNull()
                val discount = disabilityDiscount.toDoubleOrNull()

                if (peakFare == null || peakFare <= 0) {
                    message = "Error: Peak Hours Fare must be a number greater than zero"
                    return@Button
                }
                if (nonPeakFare == null || nonPeakFare <= 0) {
                    message = "Error: Non-Peak Hours Fare must be a number greater than zero"
                    return@Button
                }
                if (rainyPeakFareVal == null || rainyPeakFareVal <= 0) {
                    message = "Error: Rainy Peak Hours Fare must be a number greater than zero"
                    return@Button
                }
                if (rainyNonPeakFareVal == null || rainyNonPeakFareVal <= 0) {
                    message = "Error: Rainy Non-Peak Hours Fare must be a number greater than zero"
                    return@Button
                }
                if (discount != null && (discount < 0 || discount > 100)) {
                    message = "Error: Disability Discount must be between 0 and 100"
                    return@Button
                }

                Log.d("SetFaresScreen", "Fare values: peak=$peakHours, nonPeak=$nonPeakHours, rainyPeak=$rainingPeakFare, rainyNonPeak=$rainingNonPeakFare, discount=$disabilityDiscount")

                coroutineScope.launch {
    val matatuIdInt = matatuId.toIntOrNull()
    if (matatuIdInt == null) {
        message = "Error: Invalid Matatu ID (must be a number)"
        return@launch
    }
    // Check if fare already exists for this matatu
    val fares = fareRepository.getFaresForMatatu(matatuIdInt)
    val existingFare = fares.firstOrNull()
    val fareData = mapOf(
        "matatuId" to matatuIdInt,
        "peakFare" to peakFare,
        "nonPeakFare" to nonPeakFare,
        "rainyPeakFare" to rainyPeakFareVal,
        "rainyNonPeakFare" to rainyNonPeakFareVal,
        "disabilityDiscount" to discount
    )
    if (existingFare != null) {
        // Update existing fare
        val updatedFare = existingFare.copy(
            peakFare = peakFare ?: existingFare.peakFare,
            nonPeakFare = nonPeakFare ?: existingFare.nonPeakFare,
            rainyPeakFare = rainyPeakFareVal ?: existingFare.rainyPeakFare,
            rainyNonPeakFare = rainyNonPeakFareVal ?: existingFare.rainyNonPeakFare,
            disabilityDiscount = discount ?: existingFare.disabilityDiscount
        )
        val success = fareRepository.updateFare(existingFare.fareId, updatedFare)
        message = if (success) "✅ Fares updated successfully!" else "Error updating fares."
        if (success) {
            val intent = Intent(context, FareTabbedActivity::class.java).apply {
                putExtra("matatuId", matatuId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }
    } else {
        // No fare exists, create new
        val resultMessage = fareRepository.saveFaresRaw(fareData)
        message = resultMessage
        if (resultMessage == "✅ Fares saved successfully!") {
            val intent = Intent(context, FareTabbedActivity::class.java).apply {
                putExtra("matatuId", matatuId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
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
