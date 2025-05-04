package com.example.dynamic_fare

import android.app.Activity
import android.util.Log
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
import com.example.dynamic_fare.datastore.UserSessionDataStore
import com.google.android.gms.auth.api.identity.Identity

@Composable
fun LoginScreenContent(navController: NavController) {
    val context = LocalContext.current
    val sqliteUserRepository = SqliteUserRepository(context)
    val authRepository = AuthRepository(context, sqliteUserRepository)
    val userRepository = UserRepository(context)
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, userRepository)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loginSuccessEmail by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val oneTapClient = com.google.android.gms.auth.api.identity.Identity.getSignInClient(context)
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val googleIdToken = credential.googleIdToken

            if (googleIdToken != null) {
                authViewModel.googleSignIn(googleIdToken) { role ->
                    navigateByRole(navController, role, email)
                }
            }
        }
    }

    LaunchedEffect(loginSuccessEmail) {
        loginSuccessEmail?.let {
            UserSessionDataStore.saveUserEmail(context, it)
            // Also save role if available
            val user = sqliteUserRepository.getUserByEmail(it)
            user?.role?.let { role ->
                UserSessionDataStore.saveUserRole(context, role)
            }
        }
    }

    // --- AUTO-LOGIN LOGIC ---
    LaunchedEffect(Unit) {
        val emailFlow = UserSessionDataStore.getUserEmail(context)
        val roleFlow = UserSessionDataStore.getUserRole(context)
        emailFlow.collect { savedEmail ->
            if (!savedEmail.isNullOrEmpty()) {
                roleFlow.collect { savedRole ->
                    if (!savedRole.isNullOrEmpty()) {
                        // Double-check the user actually exists in the DB and is valid
                        val user = sqliteUserRepository.getUserByEmail(savedEmail)
                        if (user != null && user.role == savedRole) {
                            navigateByRole(navController, savedRole, savedEmail)
                        } else {
                            // Session invalid, clear and go to login
                            UserSessionDataStore.clearUserEmail(context)
                            navController.navigate(Routes.LoginScreenContent) {
                                popUpTo(Routes.LoginScreenContent) { inclusive = true }
                            }
                        }
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
                        loginSuccessEmail = email
                        navigateByRole(navController, result, email)
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
        // Button(
        //     onClick = {
        //         val signInRequest = authRepository.googleSignInRequest()
        //         val oneTapClient = com.google.android.gms.auth.api.identity.Identity.getSignInClient(context)
        //         oneTapClient.beginSignIn(signInRequest)
        //             .addOnSuccessListener { result ->
        //                 launcher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
        //             }
        //             .addOnFailureListener { e ->
        //                 Toast.makeText(context, "Google Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        //             }
        //     },
        //     colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
        //     shape = RoundedCornerShape(8.dp),
        //     modifier = Modifier.fillMaxWidth().height(50.dp)
        // ) {
        //     Text("Sign in with Google", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        // }

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
                    navController.navigate(Routes.SignUpScreen) {
                        popUpTo(Routes.LoginScreenContent) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun navigateByRole(navController: NavController, role: String?, email: String? = null) {
    Log.d("LoginNavigation", "navigateByRole called with role=$role, email=$email")
    if (email.isNullOrEmpty()) {
        Toast.makeText(navController.context, "Email not found!", Toast.LENGTH_SHORT).show()
        navController.navigate(Routes.LoginScreenContent) {
            popUpTo(Routes.LoginScreenContent) { inclusive = true }
        }
        return
    }
    when (role) {
        "Matatu Operator" -> {
            Log.d("LoginNavigation", "Navigating to operatorHomeScreen with operatorId=$email")
            navController.navigate(Routes.operatorHomeRoute(email)) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        "Matatu Client" -> {
            Log.d("LoginNavigation", "Navigating to clientHome for userId=$email")
            navController.navigate(Routes.matatuEstimateRoute(email)) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        else -> {
            Toast.makeText(navController.context, "Invalid role", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.LoginScreenContent) {
                popUpTo(Routes.LoginScreenContent) { inclusive = true }
            }
        }
    }
}

// --- HARD BLOCK: Prevent operator from ever seeing clientHome/home screens ---
@Composable
fun BlockOperatorOnClientScreens(navController: NavController, userRole: String?) {
    LaunchedEffect(userRole) {
        if (userRole == "Matatu Operator") {
            navController.navigate(Routes.operatorHomeRoute("")) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}
