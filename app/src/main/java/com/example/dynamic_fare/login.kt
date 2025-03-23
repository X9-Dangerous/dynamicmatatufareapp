package com.example.dynamic_fare

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()), // ✅ Fix disappearing
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                textStyle = TextStyle(color = Color.Black),
                placeholder = { Text("Email", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                textStyle = TextStyle(color = Color.Black),
                placeholder = { Text("Password", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Forgot Password?",
                color = Color.Blue,
                modifier = Modifier.clickable { /* Navigate to Forgot Password Screen */ }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { /* Handle Login */ },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("LOG IN", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Don't have an account? Sign Up",
                color = Color.Blue,
                modifier = Modifier.clickable { navController.navigate("signup") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}
