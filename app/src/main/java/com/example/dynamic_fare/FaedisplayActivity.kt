package com.example.dynamic_fare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.models.MatatuFares // Import updated model

class FareDisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matatuId = intent.getStringExtra("matatuId") ?: ""

        setContent {
            FareDisplayScreen(matatuId)
        }
    }
}

@Composable
fun FareDisplayScreen(matatuId: String) {
    val context = LocalContext.current
    var fareDetails by remember { mutableStateOf<MatatuFares?>(null) } // Updated to MatatuFares
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch fare details when the screen is displayed
    LaunchedEffect(matatuId) {
        FareRepository.getFareDetails(matatuId) { fetchedFares ->
            if (fetchedFares != null) {
                fareDetails = fetchedFares
            } else {
                errorMessage = "⚠️ Failed to load fare details. Please try again."
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fare Details",
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, fontSize = 16.sp)
        } else {
            fareDetails?.let { fares ->
                FareDetailItem(label = "Peak Fare:", value = fares.peakFare)
                FareDetailItem(label = "Non-Peak Fare:", value = fares.nonPeakFare)
                FareDetailItem(label = "Rainy Peak Fare:", value = fares.rainyPeakFare)
                FareDetailItem(label = "Rainy Non-Peak Fare:", value = fares.rainyNonPeakFare)
                FareDetailItem(label = "Disability Discount:", value = fares.disabilityDiscount)
            }
        }
    }
}

@Composable
fun FareDetailItem(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 18.sp, color = Color.Gray)
        Text(text = "Ksh ${"%.2f".format(value)}", fontSize = 18.sp, color = Color.Black)
    }
}
