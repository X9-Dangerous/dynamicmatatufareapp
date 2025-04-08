package com.example.dynamic_fare

import android.util.Base64
import android.util.Log
import com.example.dynamic_fare.BuildConfig
/*import com.google.firebase.BuildConf*/
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Define API Service
interface MpesaApiService {
    @Headers("Content-Type: application/json")
    @POST("mpesa/stkpush/v1/processrequest")
    fun initiatePayment(
        @Header("Authorization") authToken: String,
        @Body requestBody: PaymentRequest
    ): Call<ResponseBody>
}

// Helper data class for holding payment type details
data class PaymentTypeDetails(
    val shortCode: String,
    val transactionType: String,
    val partyA: String,
    val partyB: String
)

// Data class for STK push request
data class PaymentRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String = "CustomerPayBillOnline",
    val Amount: String,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String,
    val AccountReference: String = "DynamicMatau",
    val TransactionDesc: String = "Matau Fare Payment"
)

class MpesaService {
    private val consumerKey = BuildConfig.MPESA_CONSUMER_KEY
    private val consumerSecret = BuildConfig.MPESA_CONSUMER_SECRET
    private val businessShortCode = BuildConfig.BUSINESS_SHORT_CODE
    private val passkey = BuildConfig.PASSKEY
    private val callbackUrl = BuildConfig.CALLBACK_URL
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://sandbox.safaricom.co.ke/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private val apiService = retrofit.create(MpesaApiService::class.java)

    // Function to fetch M-Pesa access token
    private fun getMpesaToken(callback: (String?) -> Unit) {
        val auth = Base64.encodeToString(
            "$consumerKey:$consumerSecret".toByteArray(),
            Base64.NO_WRAP
        )

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
            .header("Authorization", "Basic $auth")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("MpesaService", "Error getting token: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.string()?.let {
                    val token = JSONObject(it).getString("access_token")
                    callback(token)
                }
            }
        })
    }

    // Function to initiate STK push
    fun initiateMpesaPayment(
        businessNumber: String,
        amount: Int,
        customerPhone: String,
        mpesaType: String,
        accountRef: String? = null,
        callback: (Boolean, String) -> Unit
    ) {
        getMpesaToken { token ->
            if (token == null) {
                callback(false, "Failed to get access token")
                return@getMpesaToken
            }

            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val password = Base64.encodeToString(
                "$businessShortCode$passkey$timestamp".toByteArray(),
                Base64.NO_WRAP
            )

            // Log payment details
            Log.d("MpesaService", "Payment Type: $mpesaType")
            Log.d("MpesaService", "Business Number: $businessNumber")
            Log.d("MpesaService", "Customer Phone: $customerPhone")

            val paymentDetails = when (mpesaType.lowercase()) {
                "till number" -> {
                    // For Till Number payments
                    PaymentTypeDetails(
                        shortCode = businessNumber,  // Till number
                        transactionType = "CustomerBuyGoodsOnline",
                        partyA = businessNumber,    // Business number (Till)
                        partyB = businessShortCode  // Business shortcode
                    )
                }
                "paybill" -> {
                    // For Paybill payments
                    PaymentTypeDetails(
                        shortCode = businessNumber,  // Paybill number
                        transactionType = "CustomerPayBillOnline",
                        partyA = businessNumber,    // Business number (Paybill)
                        partyB = businessShortCode  // Business shortcode
                    )
                }
                "send money" -> {
                    // For Send Money to phone
                    PaymentTypeDetails(
                        shortCode = businessShortCode,  // Default shortcode
                        transactionType = "CustomerPayBillOnline",
                        partyA = businessNumber,       // Business phone number
                        partyB = businessShortCode     // Business shortcode
                    )
                }
                "pochi la biashara" -> {
                    // For Pochi la Biashara
                    PaymentTypeDetails(
                        shortCode = businessShortCode,  // Default shortcode
                        transactionType = "CustomerPayBillOnline",
                        partyA = businessNumber,       // Business Pochi number
                        partyB = businessShortCode     // Business shortcode
                    )
                }
                else -> {
                    Log.e("MpesaService", "Invalid payment type: $mpesaType")
                    callback(false, "Invalid payment type")
                    return@getMpesaToken
                }
            }

            val requestBody = PaymentRequest(
                BusinessShortCode = paymentDetails.shortCode,
                Password = password,
                Timestamp = timestamp,
                TransactionType = paymentDetails.transactionType,
                Amount = amount.toString(),
                PartyA = paymentDetails.partyA,
                PartyB = paymentDetails.partyB,
                PhoneNumber = customerPhone,
                CallBackURL = callbackUrl,
                AccountReference = accountRef ?: "DynamicMatau",
                TransactionDesc = "Matatu Fare Payment"
            )

            apiService.initiatePayment("Bearer $token", requestBody).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        callback(true, "STK Push sent successfully!")
                    } else {
                        callback(false, "Payment failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback(false, "Network error: ${t.message}")
                }
            })
        }}
}