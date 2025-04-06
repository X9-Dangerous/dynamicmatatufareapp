package com.example.dynamic_fare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dynamic_fare.R

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                username = "John Doe",
                profilePicUrl = "https://example.com/profile.jpg", // Replace with actual image URL
                onLogout = { /* Handle logout */ }
            )
        }
    }


    private fun ProfileScreen(username: String, profilePicUrl: String, onLogout: () -> Unit) {

    }
}

@Composable
fun ClientProfileScreen(navController: NavController) {
    // Default values for the profile
    val username = "Client User"
    val profilePicUrl = ""
    
    // Function to handle logout
    val onLogout = {
        // Navigate back to login
        navController.navigate(Routes.LoginScreenContent) {
            popUpTo(0) { inclusive = true }
        }
    }
    
    ProfileScreen(
        username = username,
        profilePicUrl = profilePicUrl,
        onLogout = onLogout,
        navController = navController
    )
}

@Composable
fun ProfileScreen(
    username: String,
    profilePicUrl: String,
    onLogout: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars) ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture and Username
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                // Add padding for system bars
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Image(
                painter = if (profilePicUrl.isNotEmpty()) {
                    rememberAsyncImagePainter(profilePicUrl) // Load from URL
                } else {
                    painterResource(id = R.drawable.ic_acccount) // Default profile image
                },
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray, shape = CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = username,
                fontSize = 20.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contact Information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                // Add padding for system bars
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Contact Information",
                    fontSize = 18.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_alerts), // Replace with email icon
                        contentDescription = "Email",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "user@example.com",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_phone), // Replace with phone icon
                        contentDescription = "Phone",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "+254 712 345 678",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                // Add padding for system bars
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(text = "Logout", color = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f)) // Push footer to bottom

        // Using the shared footer from footer.kt
        FooterWithIcons(navController)
    }
}
