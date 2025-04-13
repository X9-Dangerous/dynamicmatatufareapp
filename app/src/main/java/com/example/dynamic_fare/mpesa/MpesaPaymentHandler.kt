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
        // Validate phone number
        val customerPhone = mpesaDetails["phoneNumber"]
        if (customerPhone.isNullOrEmpty()) {
            callback(false, "Please enter your M-Pesa phone number")
            return
        }

        when (mpesaOption.lowercase()) {
            "send money" -> {
                val sendMoneyPhone = mpesaDetails["sendMoneyPhone"]
                if (sendMoneyPhone.isNullOrEmpty()) {
                    callback(false, "Send Money phone number not found")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Send Money payment to: $sendMoneyPhone from: $customerPhone")
                mpesaService.initiateMpesaPayment(
                    businessNumber = sendMoneyPhone,
                    amount = amount,
                    customerPhone = customerPhone,
                    mpesaType = mpesaOption,
                    callback = { success, message -> callback(success, message) }
                )
            }

            "till number" -> {
                val tillNumber = mpesaDetails["tillNumber"]
                if (tillNumber.isNullOrEmpty()) {
                    callback(false, "Till number not found")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Till payment to: $tillNumber from: $customerPhone")
                mpesaService.initiateMpesaPayment(
                    businessNumber = tillNumber,
                    amount = amount,
                    customerPhone = customerPhone,
                    mpesaType = mpesaOption,
                    callback = { success, message -> callback(success, message) }
                )
            }

            "paybill" -> {
                val paybillNumber = mpesaDetails["paybillNumber"]
                val accountNumber = mpesaDetails["accountNumber"]
                if (paybillNumber.isNullOrEmpty()) {
                    callback(false, "Paybill number not found")
                    return
                }
                if (accountNumber.isNullOrEmpty()) {
                    callback(false, "Account number not found")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Paybill payment to: $paybillNumber, Account: $accountNumber, From: $customerPhone")
                mpesaService.initiateMpesaPayment(
                    businessNumber = paybillNumber,
                    amount = amount,
                    customerPhone = customerPhone,
                    mpesaType = mpesaOption,
                    accountRef = accountNumber,
                    callback = { success, message -> callback(success, message) }
                )
            }

            "pochi la biashara" -> {
                val pochiNumber = mpesaDetails["pochiNumber"]
                if (pochiNumber.isNullOrEmpty()) {
                    callback(false, "Pochi la Biashara number not found")
                    return
                }
                android.util.Log.d("MpesaPayment", "Initiating Pochi payment to: $pochiNumber from: $customerPhone")
                mpesaService.initiateMpesaPayment(
                    businessNumber = pochiNumber,
                    amount = amount,
                    customerPhone = customerPhone,
                    mpesaType = mpesaOption,
                    callback = { success, message -> callback(success, message) }
                )
            }

            else -> {
                callback(false, "Unsupported payment method: $mpesaOption. Must be one of: Send Money, Till Number, Paybill, or Pochi la Biashara")
            }
        }
    }
}