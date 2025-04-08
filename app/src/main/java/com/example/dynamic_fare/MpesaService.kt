package com.example.dynamic_fare

import android.util.Base64
import android.util.Log
import com.example.dynamic_fare.BuildConfig
/*import com.google.firebase.BuildConfig*/
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.http.Field
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
    val BusinessShortCode: Int,  // Changed to Int
    val Password: String,
    val Timestamp: String,
    val TransactionType: String,
    val Amount: Int,      // Changed to Int
    val PartyA: Long,     // Changed to Long for phone numbers
    val PartyB: Int,      // Changed to Int
    val PhoneNumber: Long, // Changed to Long for phone numbers
    val CallBackURL: String,
    val AccountReference: String,
    val TransactionDesc: String = "Matau Fare Payment"
)

class MpesaService {
    companion object {
        // Safaricom sandbox test credentials
        private const val businessShortCode = "174379"  // Default Safaricom test shortcode
        private const val passkey = ""
        private const val consumerKey = "2d8XRhGiGSM3vPQgNxPG3Ry0G0gDGYAA"
        private const val consumerSecret = "EudvFE3v1E1vPXMA"
        private const val callbackUrl = "https://ad08-197-254-4-166.ngrok-free.app"
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://sandbox.safaricom.co.ke/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build())
        .build()

    private val apiService = retrofit.create(MpesaApiService::class.java)

    // Function to fetch M-Pesa access token
    private fun getMpesaToken(callback: (String?) -> Unit) {
        android.util.Log.d("MpesaPaymentFlow", "=== STEP 1: GETTING ACCESS TOKEN ===")
        android.util.Log.d("MpesaPaymentFlow", "Consumer Key length: ${consumerKey.length}")
        android.util.Log.d("MpesaPaymentFlow", "Consumer Secret length: ${consumerSecret.length}")
        
        val auth = Base64.encodeToString(
            "$consumerKey:$consumerSecret".toByteArray(),
            Base64.NO_WRAP
        )
        
        android.util.Log.d("MpesaPaymentFlow", "Generated Base64 Auth: $auth")
        android.util.Log.d("MpesaPaymentFlow", "Making token request to: https://sandbox.safaricom.co.ke/oauth/v1/generate")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
            .header("Authorization", "Basic $auth")
            .build()

        android.util.Log.d("MpesaPaymentFlow", "Request Headers: ${request.headers}")

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                android.util.Log.e("MpesaPaymentFlow", "❌ Token Request Failed: ${e.message}")
                android.util.Log.e("MpesaPaymentFlow", "Error stack trace: ${e.stackTraceToString()}")
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    val responseBody = response.body?.string()
                    android.util.Log.d("MpesaPaymentFlow", "Token Response Code: ${response.code}")
                    android.util.Log.d("MpesaPaymentFlow", "Token Response Headers: ${response.headers}")
                    android.util.Log.d("MpesaPaymentFlow", "Token Response Body: $responseBody")
                    
                    if (response.isSuccessful && responseBody != null) {
                        val token = JSONObject(responseBody).getString("access_token")
                        android.util.Log.d("MpesaPaymentFlow", "✅ Successfully got access token")
                        callback(token)
                    } else {
                        android.util.Log.e("MpesaPaymentFlow", "❌ Failed to get token. Response code: ${response.code}")
                        callback(null)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MpesaPaymentFlow", "❌ Error processing token response: ${e.message}")
                    callback(null)
                }
            }
        })
    }

    // Function to initiate STK push
    private fun formatPhoneNumber(phone: String): String {
        android.util.Log.d("MpesaService", "Formatting phone number: $phone")
        // Remove any spaces, hyphens or plus signs
        var formatted = phone.replace("\\s|-|\\+".toRegex(), "")
        
        // If it starts with 0, replace with 254
        if (formatted.startsWith("0")) {
            formatted = "254${formatted.substring(1)}"
        }
        // If it doesn't start with 254, add it
        else if (!formatted.startsWith("254")) {
            formatted = "254$formatted"
        }
        
        android.util.Log.d("MpesaService", "Formatted phone number: $formatted")
        return formatted
    }

    fun initiateMpesaPayment(businessNumber: String, amount: Int, customerPhone: String, accountRef: String? = null, isTillNumber: Boolean = false, callback: (Boolean, String) -> Unit) {
        android.util.Log.d("MpesaPaymentFlow", "\n=== STEP 2: INITIATING PAYMENT ===")
        android.util.Log.d("MpesaPaymentFlow", "Input Parameters:")
        android.util.Log.d("MpesaPaymentFlow", "- Business Number: $businessNumber")
        android.util.Log.d("MpesaPaymentFlow", "- Amount: $amount")
        android.util.Log.d("MpesaPaymentFlow", "- Customer Phone: $customerPhone")
        android.util.Log.d("MpesaPaymentFlow", "- Account Ref: $accountRef")
        android.util.Log.d("MpesaPaymentFlow", "- Is Till Number: $isTillNumber")
        android.util.Log.d("MpesaPaymentFlow", "- Default Business Short Code: $businessShortCode")
        android.util.Log.d("MpesaPaymentFlow", "- Callback URL: $callbackUrl")

        getMpesaToken { token ->
            if (token == null) {
                android.util.Log.e("MpesaPaymentFlow", "❌ Cannot proceed - No access token")
                callback(false, "Failed to get access token")
                return@getMpesaToken
            }

            android.util.Log.d("MpesaPaymentFlow", "\n=== STEP 3: PREPARING STK PUSH ===")
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val shortCodeToUse = if (isTillNumber) businessNumber else businessShortCode
            
            android.util.Log.d("MpesaPaymentFlow", "=== PASSWORD GENERATION DETAILS ===")
            android.util.Log.d("MpesaPaymentFlow", "1. Components:")
            android.util.Log.d("MpesaPaymentFlow", "   Business Short Code: '$shortCodeToUse'")
            android.util.Log.d("MpesaPaymentFlow", "   Passkey: '$passkey'")
            android.util.Log.d("MpesaPaymentFlow", "   Passkey character by character:")
            passkey.forEachIndexed { index, char ->
                android.util.Log.d("MpesaPaymentFlow", "      Position ${index + 1}: '$char' (${char.code})")
            }
            android.util.Log.d("MpesaPaymentFlow", "   Timestamp: '$timestamp'")
            
            val passwordString = "$shortCodeToUse$passkey$timestamp"
            android.util.Log.d("MpesaPaymentFlow", "\n2. Combined String (before Base64):")
            android.util.Log.d("MpesaPaymentFlow", "   '$passwordString'")
            android.util.Log.d("MpesaPaymentFlow", "   Length: ${passwordString.length} characters")
            
            val password = Base64.encodeToString(
                passwordString.toByteArray(),
                Base64.NO_WRAP
            )
            
            android.util.Log.d("MpesaPaymentFlow", "\n3. Final Base64 Password:")
            android.util.Log.d("MpesaPaymentFlow", "   '$password'")
            android.util.Log.d("MpesaPaymentFlow", "   Length: ${password.length} characters")
            
            // Verify each component is correct
            android.util.Log.d("MpesaPaymentFlow", "\n4. Verification:")
            android.util.Log.d("MpesaPaymentFlow", "   Short Code correct? ${shortCodeToUse == "174379"}")
            android.util.Log.d("MpesaPaymentFlow", "   Passkey correct? ${passkey == "bfb279f9aa9bdbcf158e97dd71a467cd2c2c7f96c8b997b5e4e5af435d5e6e36"}")
            android.util.Log.d("MpesaPaymentFlow", "   Timestamp format correct? ${timestamp.matches(Regex("\\d{14}"))}")

            val formattedPhone = formatPhoneNumber(customerPhone)
            android.util.Log.d("MpesaPaymentFlow", "Formatted phone number: $formattedPhone")

            val requestBody = PaymentRequest(
                BusinessShortCode = shortCodeToUse.toInt(),
                Password = password,
                Timestamp = timestamp,
                TransactionType = if (isTillNumber) "CustomerBuyGoodsOnline" else "CustomerPayBillOnline",
                Amount = amount,
                PartyA = formattedPhone.toLong(),
                PartyB = businessNumber.toInt(),
                PhoneNumber = formattedPhone.toLong(),
                CallBackURL = callbackUrl,
                AccountReference = accountRef ?: "DynamicMatau"
            )

            android.util.Log.d("MpesaPaymentFlow", "\n=== STEP 4: SENDING STK PUSH REQUEST ===")
            android.util.Log.d("MpesaPaymentFlow", "Request Body: $requestBody")
            android.util.Log.d("MpesaPaymentFlow", "Authorization: Bearer ${token.take(10)}...")

            apiService.initiatePayment("Bearer $token", requestBody).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        android.util.Log.d("MpesaPaymentFlow", "\n=== STEP 5: PROCESSING RESPONSE ===")
                        android.util.Log.d("MpesaPaymentFlow", "Response Code: ${response.code()}")
                        android.util.Log.d("MpesaPaymentFlow", "Response Headers: ${response.headers()}")
                        
                        if (response.isSuccessful) {
                            val responseStr = response.body()?.string() ?: "No response body"
                            android.util.Log.d("MpesaPaymentFlow", "✅ Success Response: $responseStr")
                            callback(true, "STK Push sent successfully!")
                        } else {
                            val errorStr = response.errorBody()?.string() ?: "Unknown error"
                            android.util.Log.e("MpesaPaymentFlow", "❌ Error Response: $errorStr")
                            callback(false, "Payment failed: $errorStr")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MpesaPaymentFlow", "❌ Error processing response: ${e.message}")
                        android.util.Log.e("MpesaPaymentFlow", "Stack trace: ${e.stackTraceToString()}")
                        callback(false, "Error processing response: ${e.message}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    android.util.Log.e("MpesaPaymentFlow", "❌ Network Error: ${t.message}")
                    android.util.Log.e("MpesaPaymentFlow", "Stack trace: ${t.stackTraceToString()}")
                    callback(false, "Network error: ${t.message}")
                }
            })
        }
    }
}
