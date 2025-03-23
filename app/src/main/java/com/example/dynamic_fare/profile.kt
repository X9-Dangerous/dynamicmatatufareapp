package com.example.dynamic_fare


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.R

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Profile Picture
        Icon(
            painter = painterResource(id = R.drawable.ic_acccount), // Replace with your profile icon
            contentDescription = "Profile Icon",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "John Doe", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(30.dp))

        // Profile Options
        ProfileOption("Personal Info", onClick = { /* Navigate to Profile Details */ })
        Spacer(modifier = Modifier.height(20.dp))
        ProfileOption("Settings", onClick = { /* Navigate to Settings */ })
        Spacer(modifier = Modifier.height(20.dp))
        ProfileOption("Payment", onClick = { /* Navigate to Payment */ })
        Spacer(modifier = Modifier.height(20.dp))
        ProfileOption("Help Center", onClick = { /* Navigate to Help Center */ })

        Spacer(modifier = Modifier.height(100.dp))

        // Log Out Button
        Button(
            onClick = { /* Handle Logout */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Log Out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileOption(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
    }
}
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
