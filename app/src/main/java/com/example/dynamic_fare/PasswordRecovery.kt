package com.example.dynamic_fare

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.auth.SqliteUserRepository
import kotlinx.coroutines.launch

@Composable
fun PasswordRecoveryScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Reset Password",
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.text.isNotBlank() && !isProcessing) {
                    isProcessing = true
                    val userRepo = SqliteUserRepository(context)
                    val nav = navController
                    coroutineScope.launch {
                        val user = userRepo.getUserByEmail(email.text)
                        if (user != null) {
                            nav.navigate("setNewPassword/${email.text}")
                        } else {
                            Toast.makeText(context, "No account found for this email.", Toast.LENGTH_SHORT).show()
                            isProcessing = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Enter an email!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (isProcessing) "Sending..." else "Send Reset Link",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
