package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dynamic_fare.ui.components.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavController, userId: String) {
    val firestore = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }
    var profilePicUrl by remember { mutableStateOf<String?>(null) }

    val currentRoute = navController.currentDestination?.route ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
                name = doc.getString("name") ?: "Unknown"
                email = doc.getString("email") ?: "No email"
                profilePicUrl = doc.getString("profilePicUrl")
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, userId, currentRoute) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Profile Picture
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePicUrl ?: "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { navController.navigate("edit_profile/$userId") },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile Picture", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = email, fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(30.dp))

            // Profile Options
            ProfileOption("Edit Profile") { navController.navigate("edit_profile/$userId") }

            Spacer(modifier = Modifier.height(100.dp))

            // Log Out Button
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") { popUpTo("profile") { inclusive = true } }
                },
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
