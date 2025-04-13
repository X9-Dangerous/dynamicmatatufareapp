import android.util.Base64
import android.util.Log
import com.example.dynamic_fare.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Process M-Pesa payment for a user.
 *
 * @param userPhone User's phone number.
 * @param fareAmount Fare amount to be paid.
 * @param mpesaDetails M-Pesa payment details.
 */
fun processMpesaPayment(userPhone: String, fareAmount: String?, mpesaDetails: Map<String, String>?) {
    if (userPhone.isEmpty() || fareAmount.isNullOrEmpty() || mpesaDetails.isNullOrEmpty()) {
        Log.e("M-Pesa", "⚠️ Payment Error: Missing details")
        return
    }

    val paymentType = mpesaDetails["mpesaType"] ?: "Unknown"
    val payTo = mpesaDetails["mpesaNumber"] ?: mpesaDetails["paybillAccount"] ?: ""

    Log.d("M-Pesa", "💰 Initiating M-Pesa Payment")
    Log.d("M-Pesa", "📱 Paying: Ksh $fareAmount")
    Log.d("M-Pesa", "🏦 To: $paymentType - $payTo")
    Log.d("M-Pesa", "☎️ User Phone: $userPhone")

    getMpesaAccessToken { token ->
        if (token == null) {
            Log.e("M-Pesa", "❌ Failed to get access token")
            return@getMpesaAccessToken
        }

        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val businessShortCode = mpesaDetails["businessShortCode"] ?: ""
        val passkey = mpesaDetails["password"] ?: ""
        val password = Base64.encodeToString("$businessShortCode$passkey$timestamp".toByteArray(), Base64.NO_WRAP)

        val jsonObject = JSONObject().apply {
            put("BusinessShortCode", businessShortCode)
            put("Password", password)
            put("Timestamp", timestamp)
            put("TransactionType", "CustomerPayBillOnline")
            put("Amount", fareAmount)
            put("PartyA", userPhone)
            put("PartyB", payTo)
            put("PhoneNumber", userPhone)
            put("CallBackURL", "https://123b-197-136-113-30.ngrok-free.app/mpesa-callback")
            put("AccountReference", "DynamicFare")
            put("TransactionDesc", "Matatu Fare Payment")
        }

        val client = OkHttpClient()
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonObject.toString())

        val request = Request.Builder()
            .url("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("M-Pesa", "❌ Payment Request Failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Log.d("M-Pesa", "✅ M-Pesa Response: $responseData")
            }
        })
    }
}

/**
 * Get M-Pesa access token.
 *
 * @param callback Callback function to receive the access token.
 */
fun getMpesaAccessToken(callback: (String?) -> Unit) {
    val consumerKey = BuildConfig.MPESA_CONSUMER_KEY
    val consumerSecret = BuildConfig.MPESA_CONSUMER_SECRET
    val auth = Base64.encodeToString("$consumerKey:$consumerSecret".toByteArray(), Base64.NO_WRAP)

    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
        .addHeader("Authorization", "Basic $auth")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("M-Pesa", "Token Fetch Failed: ${e.message}")
            callback(null)
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            val token = JSONObject(responseBody).optString("access_token")
            callback(token)
        }
    })
}
