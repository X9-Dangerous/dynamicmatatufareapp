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
    fun initiatePayment(@Body requestBody: PaymentRequest): Call<ResponseBody>
}

// Data Class for Payment Request
data class PaymentRequest(val phone: String, val amount: Int)

// Retrofit Instance
val retrofit = Retrofit.Builder()
    .baseUrl("https://yourbackend.com") // 🔥 Replace with your backend URL
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// Function to Initiate Payment
fun initiateMpesaPayment(phone: String, amount: Int, callback: (Boolean, String) -> Unit) {
    val apiService = retrofit.create(MpesaApiService::class.java)
    val requestBody = PaymentRequest(phone, amount)

    apiService.initiatePayment(requestBody).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                Log.d("M-Pesa", "STK Push Sent Successfully!")
                callback(true, "STK Push sent successfully!")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.e("M-Pesa", "Payment Failed: $errorMsg")
                callback(false, "Payment Failed: $errorMsg")
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("M-Pesa", "Network Error: ${t.message}")
            callback(false, "Network Error: ${t.message}")
        }
    })
}
