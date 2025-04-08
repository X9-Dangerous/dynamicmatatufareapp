package com.example.dynamic_fare

import android.content.Context
import android.widget.Toast
import com.example.dynamic_fare.data.MpesaRepository

object MpesaPaymentHandler {
    fun initiatePayment(
        context: Context,
        registrationNumber: String?,
        amount: Int,
        mpesaOption: String,
        mpesaDetails: Map<String, String>,
        callback: (Boolean, String) -> Unit
    ) {
        val mpesaService = MpesaService()

        // Validate registration number
        if (registrationNumber.isNullOrEmpty()) {
            callback(false, "Matatu registration number is missing. Payment cannot proceed.")
            return
        }

        // Validate amount
        if (amount <= 0) {
            callback(false, "Invalid amount. Payment cannot proceed.")
            return
        }

        // Use the provided M-Pesa details
        android.util.Log.d("MpesaPayment", "Processing payment with option: ${mpesaOption.lowercase()}")
        android.util.Log.d("MpesaPayment", "Payment details: $mpesaDetails")
        android.util.Log.d("MpesaPayment", "Amount: $amount")
        when (mpesaOption.lowercase()) {
            "send money" -> {
                val targetPhone = mpesaDetails["sendMoneyPhone"]
                val customerPhone = mpesaDetails["phoneNumber"]
                if (targetPhone.isNullOrEmpty()) {
                    callback(false, "Target phone number not found for Send Money payment")
                    return
                }
                if (customerPhone.isNullOrEmpty()) {
                    callback(false, "Please enter your M-Pesa phone number")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Send Money payment to: $targetPhone from: $customerPhone")
                mpesaService.initiateMpesaPayment(targetPhone, amount, customerPhone = customerPhone) { success, message ->
                    callback(success, message)
                }
            }
            "till number" -> {
                android.util.Log.d("MpesaPayment", "Processing Till Number payment with details: $mpesaDetails")
                val tillNumber = mpesaDetails["tillNumber"]
                val customerPhone = mpesaDetails["phoneNumber"]
                if (tillNumber.isNullOrEmpty()) {
                    android.util.Log.e("MpesaPayment", "Till number is null or empty in details: $mpesaDetails")
                    callback(false, "Till number not found in payment details")
                    return
                }
                if (customerPhone.isNullOrEmpty()) {
                    android.util.Log.e("MpesaPayment", "Customer phone is required for Till Number payments")
                    callback(false, "Please enter your M-Pesa phone number")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Till payment to: $tillNumber from phone: $customerPhone")
                mpesaService.initiateMpesaPayment(tillNumber, amount, customerPhone = customerPhone, isTillNumber = true) { success, message ->
                    callback(success, message)
                }
            }
            "paybill" -> {
                val paybillNumber = mpesaDetails["paybillNumber"]
                val accountNumber = mpesaDetails["accountNumber"]
                val customerPhone = mpesaDetails["phoneNumber"]
                if (paybillNumber.isNullOrEmpty() || accountNumber.isNullOrEmpty()) {
                    callback(false, "Paybill or account number not found")
                    return
                }
                if (customerPhone.isNullOrEmpty()) {
                    callback(false, "Please enter your M-Pesa phone number")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Paybill payment to: $paybillNumber, Account: $accountNumber, From: $customerPhone")
                mpesaService.initiateMpesaPayment(paybillNumber, amount, customerPhone = customerPhone, accountRef = accountNumber) { success, message ->
                    callback(success, message)
                }
            }
            "pochi la biashara" -> {
                val pochiNumber = mpesaDetails["pochiNumber"]
                val customerPhone = mpesaDetails["phoneNumber"]
                if (pochiNumber.isNullOrEmpty()) {
                    callback(false, "Pochi la Biashara number not found")
                    return
                }
                if (customerPhone.isNullOrEmpty()) {
                    callback(false, "Please enter your M-Pesa phone number")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Pochi payment to: $pochiNumber from: $customerPhone")
                mpesaService.initiateMpesaPayment(pochiNumber, amount, customerPhone = customerPhone) { success, message ->
                    callback(success, message)
                }
            }
            else -> {
                callback(false, "Unsupported payment method: $mpesaOption. Must be one of: Send Money, Till Number, Paybill, or Pochi la Biashara")
            }
        }
    }
}
