package com.example.dynamic_fare

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dynamic_fare.R
import com.example.dynamic_fare.auth.SqliteUserRepository

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
fun ClientProfileScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val userRepository = remember { SqliteUserRepository(context) }
    val userEmail = userId

    var userData by remember { mutableStateOf<com.example.dynamic_fare.auth.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userEmail) {
        if (userEmail != null) {
            val user = userRepository.getUserByEmail(userEmail)
            if (user != null) {
                userData = user
                isLoading = false
            } else {
                errorMessage = "User not found in local database."
                isLoading = false
            }
        } else {
            errorMessage = "No user logged in."
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Client Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red)
        } else if (userData != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Name: ${userData!!.name}", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Phone: ${userData!!.phone}", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: ${userData!!.email}", fontSize = 16.sp)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                navController.navigate("login") {
                    popUpTo("clientHome") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Text(text = "Logout", color = Color.White)
        }
    }
}