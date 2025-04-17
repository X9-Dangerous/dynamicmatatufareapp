package com.example.dynamic_fare.models

data class Payment(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val route: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending", // pending, completed, failed
    val startLocation: String = "",
    val endLocation: String = "",
    val matatuRegistration: String = "",
    val mpesaReceiptNumber: String = "",  // Important for verification
    val paymentMethod: String = "",       // e.g., "M-Pesa Till", "M-Pesa Paybill"
    val phoneNumber: String = "",         // Phone number used for payment
    val fleetId: String = ""              // Robust linkage to fleet
)
