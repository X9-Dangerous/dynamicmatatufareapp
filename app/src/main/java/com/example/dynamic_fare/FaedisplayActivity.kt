package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
import com.google.firebase.database.FirebaseDatabase

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
    var fareDetails by remember { mutableStateOf<List<MatatuFares>?>(null) } 
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch fare details when the screen is displayed
    LaunchedEffect(matatuId) {
        if (matatuId.isEmpty()) {
            errorMessage = "⚠️ Invalid Matatu ID"
            isLoading = false
            return@LaunchedEffect
        }

        Log.d("FareDisplayScreen", "Fetching fare details for matatuId: $matatuId")
        val faresRef = FirebaseDatabase.getInstance().getReference("fares")
        faresRef.child(matatuId).get().addOnSuccessListener { snapshot ->
            val fetchedFares = if (snapshot.exists()) {
                try {
                    val fares = mutableListOf<MatatuFares>()
                    snapshot.children.forEach { child ->
                        val matatuId = child.child("matatuId").getValue(String::class.java) ?: ""
                        val peakFare = child.child("peakFare").getValue(Double::class.java) ?: 0.0
                        val nonPeakFare = child.child("nonPeakFare").getValue(Double::class.java) ?: 0.0
                        val rainyPeakFare = child.child("rainyPeakFare").getValue(Double::class.java) ?: 0.0
                        val rainyNonPeakFare = child.child("rainyNonPeakFare").getValue(Double::class.java) ?: 0.0
                        val disabilityDiscount = child.child("disabilityDiscount").getValue(Double::class.java) ?: 0.0
                        val matatuIdInt = matatuId.toIntOrNull() ?: 0
                        fares.add(MatatuFares(
                            matatuId = matatuIdInt,
                            peakFare = peakFare,
                            nonPeakFare = nonPeakFare,
                            rainyPeakFare = rainyPeakFare,
                            rainyNonPeakFare = rainyNonPeakFare,
                            disabilityDiscount = disabilityDiscount
                        ))
                    }
                    fares
                } catch (e: Exception) {
                    Log.e("FareDisplayScreen", "Error parsing fare data", e)
                    null
                }
            } else null

            if (fetchedFares != null) {
                fareDetails = fetchedFares
                Log.d("FareDisplayScreen", "Successfully loaded fare details: $fareDetails")
            } else {
                errorMessage = "⚠️ Failed to load fare details. Please try again."
                Log.e("FareDisplayScreen", "Failed to load fare details for matatuId: $matatuId")
            }
            isLoading = false
        }.addOnFailureListener { e ->
            Log.e("FareDisplayScreen", "Error loading fare details", e)
            errorMessage = "⚠️ Failed to load fare details. Please try again."
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
                fares.forEach { fare ->
                    if (fare.peakFare > 0.0) {
                        FareDetailItem(label = "Peak Fare", value = fare.peakFare, isDisabled = false)
                    }
                    if (fare.nonPeakFare > 0.0) {
                        FareDetailItem(label = "Non-Peak Fare", value = fare.nonPeakFare, isDisabled = false)
                    }
                    if (fare.rainyPeakFare > 0.0) {
                        FareDetailItem(label = "Rainy Peak Fare", value = fare.rainyPeakFare, isDisabled = false)
                    }
                    if (fare.rainyNonPeakFare > 0.0) {
                        FareDetailItem(label = "Rainy Non-Peak Fare", value = fare.rainyNonPeakFare, isDisabled = false)
                    }
                    if (fare.disabilityDiscount > 0.0) {
                        FareDetailItem(label = "Disability Discount", value = fare.disabilityDiscount, isDisabled = true)
                    }
                }
            }
        }
    }
}

@Composable
fun FareDetailItem(label: String, value: Double, isDisabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 18.sp, color = Color.Gray)
        Text(
            text = if (isDisabled) "Ksh ${"%.2f".format(value)} (Disabled)" else "Ksh ${"%.2f".format(value)}",
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}
