package com.example.dynamic_fare

import android.content.Context
import android.widget.Toast
import com.example.dynamic_fare.data.MpesaRepository

object MpesaPaymentHandler {
    fun initiatePayment(context: Context, registrationNumber: String?, amount: Int, callback: (Boolean, String) -> Unit) {
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

        // Fetch M-Pesa details for the given registration number
        MpesaRepository.getMpesaDetails(registrationNumber) { type, number, account ->
            if (type.isNullOrEmpty() || number.isNullOrEmpty()) {
                callback(false, "M-Pesa details not found for Matatu: $registrationNumber")
                return@getMpesaDetails
            }

            when (type.lowercase()) {
                "phone", "pochi" -> {
                    mpesaService.initiateMpesaPayment(number, amount) { success, message ->
                        callback(success, message)
                    }
                }
                "paybill" -> {
                    if (account.isNullOrEmpty()) {
                        callback(false, "Missing Paybill Account Reference for Matatu: $registrationNumber")
                        return@getMpesaDetails
                    }
                    mpesaService.initiateMpesaPayment(number, amount, account) { success, message ->
                        callback(success, message)
                    }
                }
                else -> {
                    callback(false, "Unsupported payment method for Matatu: $registrationNumber")
                }
            }
        }
    }
}
