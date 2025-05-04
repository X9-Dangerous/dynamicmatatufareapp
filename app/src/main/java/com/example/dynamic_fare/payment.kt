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
import com.example.dynamic_fare.auth.SqliteUserRepository
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.data.FleetRepository
import com.example.dynamic_fare.data.MatatuRepository
import com.example.dynamic_fare.PaymentRepository
import com.example.dynamic_fare.models.MatatuFares
import com.example.dynamic_fare.models.Payment

@Composable
fun PaymentPage(
    navController: NavController,
    scannedQRCode: String,
    fareManager: FareManager,
    weatherManager: WeatherManager,
    timeManager: TimeManager,
    getMatatuIdFromRegistration: (String, (Int?) -> Unit) -> Unit,
    onPaymentSuccess: () -> Unit = {},
    userId: String
) {

    android.util.Log.d("PaymentPage", "PaymentPage Composable invoked")
    val context = LocalContext.current
    var registrationNumber by remember { mutableStateOf<String?>(null) }
    var fare by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var paymentStatus by remember { mutableStateOf("") }
    var mpesaOption by remember { mutableStateOf<String?>(null) }
    var mpesaDetails by remember { mutableStateOf<Map<String, String>>(mapOf()) }
    var phoneNumber by remember { mutableStateOf("") }
    var isPhoneLoaded by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }
    var originalFare by remember { mutableStateOf<String?>(null) }
    var fleetName by remember { mutableStateOf<String?>(null) }

    // New state for success dialog
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var successAmount by remember { mutableStateOf("") }

    // Use the provided userId parameter
    android.util.Log.d("Payment", "Using userId: $userId")

    // Fetch user's phone number and accessibility settings from SQLite (not Firebase)
    LaunchedEffect(userId) {
        val userRepository = com.example.dynamic_fare.auth.SqliteUserRepository(context)
        val user = userRepository.getUserByEmail(userId)
        if (user != null) {
            phoneNumber = user.phone
            isPhoneLoaded = true
        } else {
            phoneNumber = ""
            isPhoneLoaded = false
        }
        // Fetch disability status from SQLite and set isDisabled
        val userSettingsRepo = com.example.dynamic_fare.UserSettingsRepository(context)
        val userSettings = userSettingsRepo.getUserSettings(userId)
        isDisabled = userSettings?.isDisabled ?: false
    }

    // Fetch matatu details and fleet information from SQLite
    LaunchedEffect(scannedQRCode) {
        isLoading = true
        val matatuRepository = MatatuRepository(context)
        val fleetRepository = FleetRepository(context)
        android.util.Log.d("PaymentPage", "scannedQRCode (registrationNumber): $scannedQRCode")
        // Log all matatus in the database for debug
        val allMatatus = matatuRepository.getAllMatatus()
        android.util.Log.d("PaymentPage", "Total matatus in DB: ${allMatatus.size}")
        allMatatus.forEach { 
            android.util.Log.d("PaymentPage", "DB Matatu: ${it.registrationNumber}, ID: ${it.matatuId}")
        }
        val matatu = matatuRepository.getMatatuByRegistration(scannedQRCode)
        if (matatu != null) {
            registrationNumber = matatu.registrationNumber
            // Correct fleet name logic: prefer fleetId, fallback to fleetname
            if (!matatu.fleetId.isNullOrEmpty()) {
                val fleet = fleetRepository.fetchFleetDetails(matatu.fleetId)
                fleetName = fleet?.fleetName ?: matatu.fleetname.takeIf { it.isNotEmpty() }
            } else if (!matatu.fleetname.isNullOrEmpty()) {
                fleetName = matatu.fleetname
            } else {
                fleetName = null
            }
        } else {
            registrationNumber = null
            fleetName = null
            paymentStatus = "Matatu not found."
        }
        isLoading = false
    }

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
        val matatuRepository = MatatuRepository(context)
        val matatu = matatuRepository.getMatatuByRegistration(scannedQRCode)
        if (matatu != null) {
            mpesaOption = matatu.mpesaOption

            // Get M-Pesa details based on the option
            android.util.Log.d("Payment", "Raw mpesaOption: $mpesaOption")
            when (mpesaOption?.lowercase()) {
                "pochi la biashara" -> {
                    mpesaDetails = mapOf(
                        "pochiNumber" to (matatu.pochiNumber ?: ""),
                        "phoneNumber" to formatPhoneNumber(phoneNumber)
                    )
                }
                "paybill" -> {
                    mpesaDetails = mapOf(
                        "paybillNumber" to (matatu.paybillNumber ?: ""),
                        "accountNumber" to (matatu.accountNumber ?: ""),
                        "phoneNumber" to formatPhoneNumber(phoneNumber)
                    )
                }
                "till number" -> {
                    mpesaDetails = mapOf(
                        "tillNumber" to (matatu.tillNumber ?: ""),
                        "phoneNumber" to formatPhoneNumber(phoneNumber)
                    )
                }
                "send money" -> {
                    mpesaDetails = mapOf(
                        "phoneNumber" to formatPhoneNumber(phoneNumber),
                        "sendMoneyPhone" to formatPhoneNumber(matatu.sendMoneyPhone ?: "")
                    )
                }
                else -> {
                    mpesaDetails = mapOf("phoneNumber" to formatPhoneNumber(phoneNumber))
                }
            }

            android.util.Log.d("Payment", "M-Pesa Option: $mpesaOption, Details: $mpesaDetails")

            // Get city and fetch weather
            val city = matatu.routeStart?.split(",")?.lastOrNull()?.trim() ?: "Nairobi"
            android.util.Log.d("Payment", "Fetching weather for city: $city")

            weatherManager.fetchWeather { raining ->
                if (!weatherFetched) {  // Only update if we haven't timed out
                    isRaining = raining
                    weatherFetched = true
                    weatherTimeout.removeCallbacks(weatherTimeoutRunnable)
                    android.util.Log.d("Payment", "Weather status for $city - isRaining: $raining")
                }
            }

            // Start fare fetch immediately after getting matatu details
            val fareRepository = com.example.dynamic_fare.data.FareRepository(context)
            val faresList = fareRepository.getFaresForMatatu(matatu.matatuId.toIntOrNull() ?: -1)
            if (faresList.isNotEmpty()) {
                // Use fares
                val isPeakHours = timeManager.isPeakHours()
                val (baseFare, breakdown) = fareManager.getFare(faresList, isPeakHours, isRaining, isDisabled, false)

                // Store original and final fares
                originalFare = baseFare.toInt().toString()
                fare = baseFare.toInt().toString()
                android.util.Log.d("Payment", "Original fare: $originalFare, Final fare: $fare")

                // Use the breakdown directly since getFare() now includes the disability discount info
                val finalBreakdown = breakdown

                paymentStatus = finalBreakdown
                android.util.Log.d("Payment", "Original fare: $originalFare, Final fare (after disability check): $fare")
                isLoading = false
            } else {
                paymentStatus = "No fare data available."
                isLoading = false
            }
        } else {
            isLoading = false
            paymentStatus = "Matatu not found."
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onPaymentSuccess()
                navController.navigate(Routes.matatuEstimateRoute(userId)) {
                    popUpTo(Routes.matatuEstimateRoute(userId)) { inclusive = true }
                }
            },
            title = { Text("Payment Successful") },
            text = { Text("Your payment of KES $successAmount has been processed successfully.\n$successMessage") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPaymentSuccess()
                        navController.navigate(Routes.matatuEstimateRoute(userId)) {
                            popUpTo(Routes.matatuEstimateRoute(userId)) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B51F5))
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
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
        } else if (registrationNumber == null) {
            Text(
                text = "Matatu details not found",
                color = Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (fare == null) {
            Text(
                text = "Fare details not available",
                color = Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            // Payment Details
            Text(
                text = "Payment Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            PaymentDetailRow("Matatu Registration", registrationNumber!!)
            if (!fleetName.isNullOrEmpty()) {
                PaymentDetailRow("Fleet", fleetName!!)
            }
            Spacer(modifier = Modifier.height(20.dp))
            PaymentDetailRow("Fare", "KES $fare")
            Spacer(modifier = Modifier.height(20.dp))
            // Show original and discounted fare if user is disabled and there's a discount
            if (isDisabled && originalFare != null && fare != originalFare) {
                PaymentDetailRow("Original Fare", "KES $originalFare")
                PaymentDetailRow("Disability Discount Applied", "")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            PaymentDetailRow("Payment method", when(mpesaOption?.lowercase()) {
                "pochi la biashara" -> "M-Pesa Pochi la Biashara"
                "paybill" -> "M-Pesa Paybill"
                "till" -> "M-Pesa Till"
                "send money" -> "M-Pesa Send Money"
                else -> "M-Pesa"
            })

            Spacer(modifier = Modifier.height(20.dp))

            // Display phone number
            PaymentDetailRow(
                label = "Your M-Pesa Number",
                value = phoneNumber
            )

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
                    if (!isPhoneLoaded || phoneNumber.isEmpty()) {
                        paymentStatus = "Please wait, loading your M-Pesa number..."
                        return@Button
                    }

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

                    val detailsWithPhone = mpesaDetails.toMutableMap().apply {
                        put("phoneNumber", formatPhoneNumber(phoneNumber))
                    }
                    android.util.Log.d("Payment", "Making payment with details: $detailsWithPhone")

                    MpesaPaymentHandler.initiatePayment(
                        context = context,
                        registrationNumber = registrationNumber!!,
                        amount = validFare,
                        mpesaOption = mpesaOption!!,
                        mpesaDetails = detailsWithPhone,
                        callback = { success, message ->
                            isLoading = false
                            paymentStatus = message

                            if (success) {
                                // Save payment to history
                                val payment = Payment(
                                    id = java.util.UUID.randomUUID().toString(),
                                    userId = userId,
                                    amount = validFare.toDouble(),
                                    route = scannedQRCode,
                                    timestamp = System.currentTimeMillis(),
                                    status = "completed",
                                    startLocation = "Current Location",  // You can update this with actual locations
                                    endLocation = "Destination",
                                    matatuRegistration = registrationNumber.toString(),
                                    mpesaReceiptNumber = message.substringAfter("Receipt number: ").takeIf { message.contains("Receipt number:") } ?: "",
                                    paymentMethod = mpesaOption.toString(),
                                    phoneNumber = phoneNumber
                                )

                                // Save payment to local SQLite database
                                val paymentRepo = PaymentRepository(context)
                                paymentRepo.insertPayment(payment)
                                android.util.Log.d("Payment", "Payment saved to history (SQLite)")
                                // Update dialog state instead of showing dialog directly
                                successAmount = validFare.toString()
                                successMessage = message
                                showSuccessDialog = true
                            }
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B51F5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(50.dp),
                enabled = fare != null && !isLoading && isPhoneLoaded && phoneNumber.isNotEmpty()
            ) {
                Text(text = "PAY", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helper function to format phone number for M-Pesa
private fun formatPhoneNumber(phone: String): String {
    // Remove any non-digit characters
    val digitsOnly = phone.replace(Regex("[^0-9]"), "")

    // If it starts with 254, use as is
    if (digitsOnly.startsWith("254")) {
        return digitsOnly
    }

    // If it starts with 0, replace with 254
    if (digitsOnly.startsWith("0")) {
        return "254${digitsOnly.substring(1)}"
    }

    // If it's just 9 digits (without country code), add 254
    if (digitsOnly.length == 9) {
        return "254$digitsOnly"
    }

    // Return original digits if none of the above match
    return digitsOnly
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