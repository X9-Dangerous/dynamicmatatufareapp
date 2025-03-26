package com.example.dynamic_fare

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dynamic_fare.auth.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LoginScreenContent(navController: NavController) {
    val context = LocalContext.current
    val authRepository = AuthRepository(context)
    val userRepository = UserRepository()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, userRepository)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val oneTapClient = com.google.android.gms.auth.api.identity.Identity.getSignInClient(context)
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val googleIdToken = credential.googleIdToken

            if (googleIdToken != null) {
                authViewModel.googleSignIn(googleIdToken) { role ->
                    navigateByRole(navController, role)
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

        Spacer(modifier = Modifier.height(40.dp))

        // 🔹 Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter your password", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 🔹 Login Button with Error Handling
        Button(
            onClick = {
                authViewModel.loginUser(email, password) { result ->
                    if (result == "Matatu Operator" || result == "Matatu Client") {
                        navigateByRole(navController, result)
                    } else {
                        errorMessage = result // Show error message
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B51E0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("LOG IN", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // 🔹 Show Error Message if Exists
        if (errorMessage != null) {
            Text(
                errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Google Sign-In Button
        Button(
            onClick = {
                val signInRequest = authRepository.googleSignInRequest()
                val oneTapClient = com.google.android.gms.auth.api.identity.Identity.getSignInClient(context)
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        launcher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Google Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Sign in with Google", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Forgot Password
        Text(
            text = "Forgot password?",
            fontSize = 14.sp,
            color = Color.Blue,
            modifier = Modifier.clickable {
                navController.navigate(Routes.PasswordRecoveryScreen)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Sign-up Navigation
        Row {
            Text(text = "Don't have an account?", fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.width(4.dp))
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

fun navigateByRole(navController: NavController, role: String?) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    if (role == "Matatu Operator" && userId != null) {
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("operatorId")

        db.get().addOnSuccessListener { snapshot ->
            val operatorId = snapshot.value as? String
            if (!operatorId.isNullOrEmpty()) {
                navController.navigate("operatorHome/$operatorId")
                {
                    popUpTo(Routes.LoginScreenContent) { inclusive = true }
                }
            } else {
                Toast.makeText(navController.context, "Operator ID not found!", Toast.LENGTH_SHORT).show()
            }
        }
    } else if (role == "Matatu Client") {
        navController.navigate(Routes.MatatuEstimateScreen) {
            popUpTo(Routes.LoginScreenContent) { inclusive = true }
        }
    } else {
        Toast.makeText(navController.context, "Redirecting to homepage", Toast.LENGTH_SHORT).show()
        navController.navigate(Routes.LoginScreenContent) {
            popUpTo(Routes.LoginScreenContent) { inclusive = true }
        }
    }
}

