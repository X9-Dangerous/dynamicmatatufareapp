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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.auth.SqliteUserRepository
import kotlinx.coroutines.launch

@Composable
fun SetNewPasswordScreen(navController: NavController, email: String) {
    val context = LocalContext.current
    val userRepo = SqliteUserRepository(context)
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Set New Password",
            fontSize = 24.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            placeholder = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (newPassword.length < 6) {
                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isProcessing = true
                scope.launch {
                    userRepo.updatePasswordByEmail(email, newPassword)
                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    isProcessing = false
                    navController.navigate(Routes.LoginScreenContent) {
                        popUpTo(Routes.LoginScreenContent) { inclusive = true }
                    }
                }
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(
                text = if (isProcessing) "Updating..." else "Update Password",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
