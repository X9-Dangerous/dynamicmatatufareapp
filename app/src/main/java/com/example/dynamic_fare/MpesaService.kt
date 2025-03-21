package com.example.dynamicmataufareapp


import android.util.Base64
import android.util.Log
import com.example.dynamicmataufareapp.BuildConfig
/* import com.google.firebase.BuildConfig */
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
                Log.e("MpesaService", "Error getting token: ${'$'}{e.message}")
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
    fun initiateMpesaPayment(phone: String, amount: Int, callback: (Boolean, String) -> Unit) {
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

            val requestBody = PaymentRequest(
                BusinessShortCode = businessShortCode,
                Password = password,
                Timestamp = timestamp,
                Amount = amount.toString(),
                PartyA = phone,
                PartyB = businessShortCode,
                PhoneNumber = phone,
                CallBackURL = callbackUrl
            )

            apiService.initiatePayment("Bearer $token", requestBody).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        callback(true, "STK Push sent successfully!")
                    } else {
                        callback(false, "Payment failed: ${'$'}{response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback(false, "Network error: ${'$'}{t.message}")
                }
            })
        }
    }
}
