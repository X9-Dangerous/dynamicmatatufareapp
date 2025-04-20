package com.example.dynamic_fare

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.dynamic_fare.auth.SqliteUserRepository
import com.example.dynamic_fare.auth.User
import com.example.dynamic_fare.ui.components.BottomNavigationBar
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import android.widget.Toast
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val userRepository = remember { SqliteUserRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var userData by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        Log.d("ProfileScreen", "Searching for userId/email: $userId")
        Toast.makeText(context, "Searching for: $userId", Toast.LENGTH_SHORT).show()
        if (userId.isNotEmpty()) {
            coroutineScope.launch {
                try {
                    val user = userRepository.getUserByEmail(userId)
                    if (user != null) {
                        userData = user
                    } else {
                        error = "User not found."
                    }
                } catch (e: Exception) {
                    error = "Error: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
            error = "No user ID provided."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = "profile",
                userId = userId
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                userData?.let { user ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileSection("Name", user.name)
                        ProfileSection("Email", user.email)
                        // Add more fields here if needed

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                // Navigate to login and clear the back stack
                                navController.navigate("login") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Log Out")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}