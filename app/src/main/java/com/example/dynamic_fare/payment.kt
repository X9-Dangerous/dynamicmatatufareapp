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
    getMatatuIdFromRegistration: (String, (String?) -> Unit) -> Unit,  // Use the passed function here
    onPaymentSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var registrationNumber by remember { mutableStateOf<String?>(null) }
    var fare by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var paymentStatus by remember { mutableStateOf("") }

    // Fetch Matatu details based on QR code
    LaunchedEffect(scannedQRCode) {
        val dbRef = FirebaseDatabase.getInstance().getReference("matatus").child(scannedQRCode)
        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                registrationNumber = snapshot.child("registration").value?.toString()
            }
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
            paymentStatus = "Failed to fetch data from database."
        }
    }

    // Fetch Fare details based on scanned QR code and operatorId
    LaunchedEffect(scannedQRCode) {
        if (registrationNumber != null) {
            val matatuRegNo = registrationNumber!!
            getMatatuIdFromRegistration(matatuRegNo) { matatuId ->
                if (matatuId != null) {
                    fareManager.fetchFares(matatuId) { fares ->
                        if (fares != null) {
                            weatherManager.fetchWeather("Nairobi") { isRaining ->
                                val isPeakHours = timeManager.isPeakHours()
                                val (finalFare, breakdown) = fareManager.getFare(fares, isPeakHours, isRaining, false, false)
                                fare = finalFare.toString()
                                paymentStatus = breakdown
                            }
                        } else {
                            paymentStatus = "No fare data available."
                        }
                    }
                } else {
                    paymentStatus = "Matatu not found with the given registration number."
                }
            }
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
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
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
            PaymentDetailRow("Payment method", "M-Pesa")

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

                    val validFare = fare?.toIntOrNull() ?: 0

                    MpesaPaymentHandler.initiatePayment(context, registrationNumber!!, validFare) { success, message ->
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
