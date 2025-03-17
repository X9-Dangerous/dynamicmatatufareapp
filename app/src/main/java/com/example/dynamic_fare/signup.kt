package com.example.dynamic_fare


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignUpScreen()
        }
    }
}

@Composable
fun SignUpScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = "✦ Sign Up",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Sign up buttons
        Button(
            onClick = { /* TODO: Handle Google sign-up */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "SIGN UP WITH GOOGLE", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(40.dp))


        // Input Fields
        var name by remember { mutableStateOf("") }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        var email by remember { mutableStateOf("") }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Your E-mail") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        var password by remember { mutableStateOf("") }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("At least 8 characters") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Checkboxes
        var isDisabled by remember { mutableStateOf(false) }
        var isMatatuUser by remember { mutableStateOf(false) }
        var isMatatuClient by remember { mutableStateOf(false) }
        var termsAccepted by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isDisabled,
                    onCheckedChange = { isDisabled = it }
                )
                Text(text = "I am a person with a disability.")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isMatatuUser,
                    onCheckedChange = { isMatatuUser = it }
                )
                Text(text = "Are you a matatu user?")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isMatatuClient,
                    onCheckedChange = { isMatatuClient = it }
                )
                Text(text = "Are you a matatu client?")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it }
                )
                Text(text = "I agree to all the Terms, Privacy Policy and Fees.")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Continue Button
        Button(
            onClick = { /* TODO: Handle continue action */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B51E0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "CONTINUE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Login Link
        Text(
            text = "Have an account? Log in",
            fontSize = 16.sp,
            color = Color.Blue,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { /* TODO: Navigate to Login */ }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview(){
    SignUpScreen()
}
