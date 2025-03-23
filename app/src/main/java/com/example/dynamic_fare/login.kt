package com.example.dynamic_fare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity

@Composable
fun LoginScreenContent(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    // Initialize the Firebase Realtime Database reference for users
    val database = FirebaseDatabase.getInstance().getReference("users")

    // Function to fetch user role from Firebase
    fun fetchUserRole(uid: String, onResult: (String?) -> Unit) {
        database.child(uid).child("role").get().addOnSuccessListener { snapshot ->
            onResult(snapshot.getValue(String::class.java))
        }.addOnFailureListener { exception ->
            Log.e("FirebaseDB", "Error fetching user role: ${exception.message}")
            onResult(null)
        }
    }

    // Function to decide navigation based on role
    fun navigateByRole(role: String?) {
        when (role) {
            "operator" -> navController.navigate("operatorHome") {
                popUpTo("login") { inclusive = true }
            }
            "client" -> navController.navigate("clientHome") {
                popUpTo("login") { inclusive = true }
            }
            else -> {
                Log.e("LoginScreen", "Undefined or null user role")
                Toast.makeText(context, "Undefined or null user role", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val oneTapClient = Identity.getSignInClient(context)
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val googleIdToken = credential.googleIdToken

            if (googleIdToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("GoogleAuth", "Sign-in successful!")
                            // After successful Google sign-in, fetch the user's role
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                fetchUserRole(uid) { role ->
                                    navigateByRole(role)
                                }
                            } else {
                                Log.e("GoogleAuth", "UID is null after sign-in")
                            }
                        } else {
                            Log.e("GoogleAuth", "Sign-in failed: ${task.exception?.message}")
                            Toast.makeText(context, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Welcome Back!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please enter your details",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(50.dp))

        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        var password by remember { mutableStateOf("") }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter your password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(100.dp))
        val loginMessage = remember { mutableStateOf("") }

        Button(
            onClick = {
                Log.d("LoginScreen", "Login button clicked")
                // Step 1: Validate input
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                // Step 2: Try Firebase email/password sign-in
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseAuth", "Login Successful")
                            loginMessage.value = "Login Successful!"
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                // Fetch user role from the database after login
                                fetchUserRole(uid) { role ->
                                    navigateByRole(role)
                                }
                            } else {
                                Log.e("FirebaseAuth", "UID is null after login")
                            }
                        } else {
                            Log.e("FirebaseAuth", "Login Failed: ${task.exception?.message}")
                            loginMessage.value = task.exception?.message ?: "Login Failed!"
                            // Step 3: Show failure feedback to the user
                            Toast.makeText(context, loginMessage.value, Toast.LENGTH_SHORT).show()
                        }
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B51E0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "LOG IN",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                val signInRequest = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId("993474475969-3a9kscepn460n60m27qfbckfg37jr1i2.apps.googleusercontent.com")
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()

                val oneTapClient = Identity.getSignInClient(context)
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        launcher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                    }
                    .addOnFailureListener { e ->
                        Log.e("GoogleAuth", "Google Sign-in failed: ${e.message}")
                        Toast.makeText(context, "Google Sign-in failed.", Toast.LENGTH_SHORT).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Sign in with Google",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Forgot password?",
            fontSize = 14.sp,
            color = Color.Blue,
            modifier = Modifier.clickable { /* TODO: Implement forgot password */ }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row {
            Text(text = "Don't have an account?", fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
            // Navigate to the Sign Up page when clicked
            Text(
                text = "Sign up",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenContentPreview() {
    LoginScreenContent(rememberNavController())
}
