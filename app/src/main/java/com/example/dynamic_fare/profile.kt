package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.dynamic_fare.auth.UserRepository
import com.example.dynamic_fare.auth.UserData
import com.example.dynamic_fare.auth.OperatorData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userId: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userRepository = remember { UserRepository() }
    val coroutineScope = rememberCoroutineScope()
    
    var userData by remember { mutableStateOf<UserData?>(null) }
    var operatorData by remember { mutableStateOf<OperatorData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var profilePicUrl by remember { mutableStateOf<String?>(null) }

    // Check if user is authenticated
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
        return
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            coroutineScope.launch {
                // Use the currently authenticated user's ID
                val targetUserId = if (currentUser.uid == userId) userId else currentUser.uid
                
                try {
                    // Fetch user data
                    userRepository.fetchUserData(targetUserId).onSuccess { user ->
                        userData = user
                        profilePicUrl = user.profilePicUrl
                        
                        // If user is an operator, fetch operator details
                        if (user.role.equals("operator", ignoreCase = true)) {
                            userRepository.fetchOperatorData(targetUserId).onSuccess { operator ->
                                operatorData = operator
                            }.onFailure { e ->
                                error = "Failed to load operator data: ${e.message}"
                            }
                        }
                    }.onFailure { e ->
                        error = "Failed to load user data: ${e.message}"
                    }
                } catch (e: Exception) {
                    error = "Error: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigateUp() }) {
                        Text("Go Back")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profilePicUrl ?: "")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { /* TODO: Implement profile picture update */ },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile Picture",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Information
                    userData?.let { user ->
                        ProfileSection("Name", user.name)
                        ProfileSection("Email", user.email)
                        ProfileSection("Phone", user.phoneNumber)
                        ProfileSection("Role", user.role)
                        
                        // Show operator-specific information if role is operator
                        if (user.role.equals("operator", ignoreCase = true)) {
                            operatorData?.let { operator ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Business Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ProfileSection("Business Name", operator.businessName)
                                ProfileSection("Business Address", operator.businessAddress)
                                ProfileSection("License Number", operator.licenseNumber)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Logout Button
                    Button(
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") { popUpTo("profile") { inclusive = true } }
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

@Composable
private fun ProfileSection(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        Divider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}