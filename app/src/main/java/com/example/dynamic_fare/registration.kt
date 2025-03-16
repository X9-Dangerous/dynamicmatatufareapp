package com.example.dynamic_fare


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dynamicmataufareapp.R

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistrationScreen()
        }
    }
}

@Composable
fun RegistrationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        // Top Bar with Back Arrow and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { /* Handle back action */ }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Registration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Registration Number Input (Aligned to the left edge)
        Text(
            text = "Registration Number",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = "",
            onValueChange = { /* Handle Input */ },
            placeholder = { Text("Enter Registration Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Route Input (Aligned to the left edge)
        Text(
            text = "Route",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = "",
            onValueChange = { /* Handle Input */ },
            placeholder = { Text("Enter Route") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(100.dp)) // Moves button slightly up

        // Generate QR Code Button
        Button(
            onClick = { /* Handle QR Code Generation */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B51E0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "GENERATE QR CODE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen()
}
