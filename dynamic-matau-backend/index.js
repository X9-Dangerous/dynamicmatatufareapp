package com.example.dynamic_fare

import android.util.Log
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Define API Service
interface MpesaApiService {
    @POST("/stkpush")
    fun initiatePayment(@Body requestBody: PaymentRequest): Call<ResponseData>
}

// Data Class for Payment Request
data class PaymentRequest(val phone: String, val amount: Int)

// Data Class for Response
data class ResponseData(val success: Boolean, val message: String)

// Retrofit Instance
val retrofit = Retrofit.Builder()
    .baseUrl("https://f224-197-254-4-166.ngrok-free.app/")  // Ensure it ends with "/"
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// Function to Initiate Payment
fun initiateMpesaPayment(phone: String, amount: Int, callback: (Boolean, String) -> Unit) {
    val apiService = retrofit.create(MpesaApiService::class.java)
    val requestBody = PaymentRequest(phone, amount)

    apiService.initiatePayment(requestBody).enqueue(object : Callback<ResponseData> {
        override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
            if (response.isSuccessful) {
                Log.d("M-Pesa", "STK Push Sent Successfully!")
                callback(true, "STK Push sent successfully!")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("M-Pesa", "Payment Failed: $errorMsg")
                callback(false, "Payment Failed: $errorMsg")
            }
        }

        override fun onFailure(call: Call<ResponseData>, t: Throwable) {
            Log.e("M-Pesa", "Network Error: ${t.message}")
            callback(false, "Network Error: ${t.message}")
        }
    })
}
