package com.example.dynamic_fare.mpesa

object MpesaConfig {
    // Safaricom Daraja API credentials
    const val CONSUMER_KEY = "your_consumer_key"
    const val CONSUMER_SECRET = "your_consumer_secret"
    
    // M-Pesa API endpoints
    const val ACCESS_TOKEN_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials"
    const val STK_PUSH_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest"
    
    // Business details
    const val BUSINESS_SHORT_CODE = "174379" // Your Lipa Na M-Pesa shortcode
    const val PASSKEY = "your_passkey"
    const val CALLBACK_URL = "https://123b-197-136-113-30.ngrok-free.app/mpesa-callback"
    const val TRANSACTION_TYPE = "CustomerPayBillOnline"
    const val ACCOUNT_REFERENCE = "DynamicFare"
    const val TRANSACTION_DESC = "Payment for matatu fare"
}
