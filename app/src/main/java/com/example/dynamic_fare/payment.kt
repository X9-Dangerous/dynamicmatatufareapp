package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase

@Composable
fun PaymentPage(
    navController: NavController,
    scannedQRCode: String,
    fareManager: FareManager,
    weatherManager: WeatherManager,
    timeManager: TimeManager,
    getMatatuIdFromRegistration: (String, (String?) -> Unit) -> Unit,
    onPaymentSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var registrationNumber by remember { mutableStateOf<String?>(null) }
    var fare by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var paymentStatus by remember { mutableStateOf("") }
    var mpesaOption by remember { mutableStateOf<String?>(null) }
    var mpesaDetails by remember { mutableStateOf<Map<String, String>>(mapOf()) }

    // Fetch all required data in parallel
    LaunchedEffect(scannedQRCode) {
        // Start weather fetch with timeout
        var weatherFetched = false
        var isRaining = false
        val weatherTimeout = android.os.Handler(android.os.Looper.getMainLooper())
        val weatherTimeoutRunnable = Runnable {
            if (!weatherFetched) {
                weatherFetched = true  // Force completion after timeout
                android.util.Log.d("Payment", "Weather fetch timed out")
            }
        }
        
        // Set 2-second timeout for weather fetch
        weatherTimeout.postDelayed(weatherTimeoutRunnable, 2000)

        // Get matatu details
        val dbRef = FirebaseDatabase.getInstance().getReference("matatus").child(scannedQRCode)
        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                registrationNumber = snapshot.child("registrationNumber").value?.toString()
                mpesaOption = snapshot.child("mpesaOption").value?.toString()
                
                // Get M-Pesa details based on the option
                android.util.Log.d("Payment", "Raw mpesaOption: $mpesaOption")
                when (mpesaOption?.lowercase()) {
                    "pochi la biashara" -> {
                        mpesaDetails = mapOf(
                            "pochiNumber" to (snapshot.child("pochiNumber").value?.toString() ?: "")
                        )
                    }
                    "paybill" -> {
                        mpesaDetails = mapOf(
                            "paybillNumber" to (snapshot.child("paybillNumber").value?.toString() ?: ""),
                            "accountNumber" to (snapshot.child("accountNumber").value?.toString() ?: "")
                        )
                    }
                    "till" -> {
                        mpesaDetails = mapOf(
                            "tillNumber" to (snapshot.child("tillNumber").value?.toString() ?: "")
                        )
                    }
                    "send money" -> {
                        mpesaDetails = mapOf(
                            "phoneNumber" to (snapshot.child("sendMoneyPhone").value?.toString() ?: "")
                        )
                    }
                }
                
                android.util.Log.d("Payment", "M-Pesa Option: $mpesaOption, Details: $mpesaDetails")
                
                // Get city and fetch weather
                val city = snapshot.child("routeStart").value?.toString()?.split(",")?.lastOrNull()?.trim() ?: "Nairobi"
                android.util.Log.d("Payment", "Fetching weather for city: $city")
                
                weatherManager.fetchWeather(city) { raining ->
                    if (!weatherFetched) {  // Only update if we haven't timed out
                        isRaining = raining
                        weatherFetched = true
                        weatherTimeout.removeCallbacks(weatherTimeoutRunnable)
                        android.util.Log.d("Payment", "Weather status for $city - isRaining: $raining")
                    }
                }
                
                // Start fare fetch immediately after getting matatu details
                fareManager.fetchFares(scannedQRCode) { fares ->
                    if (fares != null) {
                        // Check if weather is ready, if not wait briefly
                        var attempts = 0
                        val maxAttempts = 3  // Reduce max attempts
                        val checkWeather = object : Runnable {
                            override fun run() {
                                if (weatherFetched || attempts >= maxAttempts) {
                                    val isPeakHours = timeManager.isPeakHours()
                                    val (finalFare, breakdown) = fareManager.getFare(fares, isPeakHours, isRaining, false, false)
                                    // Round to nearest whole number to avoid decimal issues
                                    fare = finalFare.toInt().toString()
                                    paymentStatus = breakdown
                                    isLoading = false
                                    android.util.Log.d("Payment", "Final fare amount: $fare")
                                } else {
                                    attempts++
                                    android.os.Handler(android.os.Looper.getMainLooper())
                                        .postDelayed(this, 200) // Check every 200ms
                                }
                            }
                        }
                        checkWeather.run()
                    } else {
                        paymentStatus = "No fare data available."
                        isLoading = false
                    }
                }
            } else {
                isLoading = false
                paymentStatus = "Matatu not found."
            }
        }.addOnFailureListener {
            isLoading = false
            paymentStatus = "Failed to fetch data from database."
        }
    }

    // UI Elements
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                contentDescription = "Close",
                tint = Color.DarkGray,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Payment",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading payment details...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else if (registrationNumber == null || fare == null) {
            Text(
                text = "Matatu details not available",
                color = Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            // Payment Details
            PaymentDetailRow("Matatu Registration", registrationNumber!!)
            Spacer(modifier = Modifier.height(20.dp))
            PaymentDetailRow("Fare", "KES $fare")
            Spacer(modifier = Modifier.height(20.dp))
            PaymentDetailRow("Payment method", when(mpesaOption?.lowercase()) {
                "pochi la biashara" -> "M-Pesa Pochi la Biashara"
                "paybill" -> "M-Pesa Paybill"
                "till" -> "M-Pesa Till"
                "send money" -> "M-Pesa Send Money"
                else -> "M-Pesa"
            })

            Spacer(modifier = Modifier.weight(1f))

            // Payment Status Message
            if (paymentStatus.isNotEmpty()) {
                Text(
                    text = paymentStatus,
                    color = if (paymentStatus.contains("success", true)) Color.Green else Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Pay Button
            Button(
                onClick = {
                    isLoading = true
                    paymentStatus = "Processing payment..."

                    // Convert fare string to integer, removing any non-numeric characters
                    val fareString = fare?.replace(Regex("[^0-9]"), "") ?: "0"
                    val validFare = fareString.toIntOrNull() ?: 0
                    
                    android.util.Log.d("Payment", "Initiating payment with amount: $validFare")
                    if (validFare <= 0) {
                        isLoading = false
                        paymentStatus = "Invalid fare amount"
                        return@Button
                    }

                    if (mpesaOption == null) {
                        isLoading = false
                        paymentStatus = "M-Pesa payment option not configured"
                        return@Button
                    }

                    MpesaPaymentHandler.initiatePayment(
                        context = context,
                        registrationNumber = registrationNumber!!,
                        amount = validFare,
                        mpesaOption = mpesaOption!!,
                        mpesaDetails = mpesaDetails
                    ) { success, message ->
                        isLoading = false
                        paymentStatus = message

                        if (success) {
                            onPaymentSuccess()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B51F5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(50.dp),
                enabled = fare != null && !isLoading
            ) {
                Text(text = "PAY", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PaymentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = Color.Black)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
