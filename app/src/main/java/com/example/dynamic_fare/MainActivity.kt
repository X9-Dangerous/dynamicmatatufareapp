package com.example.dynamic_fare

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DynamicMatauFareAppTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    message = if (task.isSuccessful) "Login Successful" else task.exception?.message ?: "Login Failed"
                }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { /* Navigate to Signup */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun QRCodeScreen() {
    var registrationNumber by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference
    var isOperator by remember { mutableStateOf(false) }

    // Check if the logged-in user is an operator
    LaunchedEffect(userId) {
        userId?.let {
            database.child("users").child(it).child("role")
                .get().addOnSuccessListener { snapshot ->
                    isOperator = snapshot.value == "operator"
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isOperator) {
            OutlinedTextField(
                value = registrationNumber,
                onValueChange = { registrationNumber = it },
                label = { Text("Matatu Registration Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = route,
                onValueChange = { route = it },
                label = { Text("Route") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                val qrData = "$registrationNumber - $route"
                qrBitmap = generateQRCode(qrData)
                saveQRCodeToFirebase(registrationNumber, route)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Generate QR Code")
            }
            Spacer(modifier = Modifier.height(20.dp))
            qrBitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code") }
        } else {
            Text(
                text = "Access Denied: Only Matatu Operators Can Generate QR Codes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Function to store QR Code details in Firebase
fun saveQRCodeToFirebase(registrationNumber: String, route: String) {
    val database = FirebaseDatabase.getInstance().reference
    val qrData = mapOf(
        "registrationNumber" to registrationNumber,
        "route" to route,
        "timestamp" to System.currentTimeMillis()
    )
    database.child("qr_codes").push().setValue(qrData)
}

// QR Code Generation Function
fun generateQRCode(data: String): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 400, 400)
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun PaymentScreen() {
    var phoneNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("M-Pesa Payment", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            val paymentData = hashMapOf(
                "phone_number" to phoneNumber,
                "amount" to amount,
                "status" to "Pending"
            )
            db.collection("payments").add(paymentData)
                .addOnSuccessListener {
                    statusMessage = "Payment request sent"
                }
                .addOnFailureListener {
                    statusMessage = "Error: ${it.message}"
                }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Pay with M-Pesa")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = statusMessage, style = MaterialTheme.typography.bodyLarge)
    }
}
