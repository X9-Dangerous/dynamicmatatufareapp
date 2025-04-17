package com.example.dynamic_fare

import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userId: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userRepository = remember { UserRepository() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var userData by remember { mutableStateOf<UserData?>(null) }
    var operatorData by remember { mutableStateOf<OperatorData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var profilePicUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingImage = true
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("profile_images/${currentUser?.uid}.jpg")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Update the profile URL in the database
                        val userRef = FirebaseDatabase.getInstance().getReference("users/${currentUser?.uid}")
                        userRef.child("profilePicUrl").setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                profilePicUrl = downloadUri.toString()
                                Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to update profile URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    isUploadingImage = false
                }
        }
    }

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
                val targetUserId = if (currentUser.uid == userId) userId else currentUser.uid

                try {
                    android.util.Log.d("Profile", "Fetching user data for ID: $targetUserId")
                    userRepository.fetchUserData(targetUserId).onSuccess { user ->
                        android.util.Log.d("Profile", "User data fetched: ${user.name}, phone: ${user.phoneNumber}")
                        userData = user
                        profilePicUrl = user.profilePicUrl

                        if (user.role.equals("operator", ignoreCase = true)) {
                            android.util.Log.d("Profile", "User is an operator, fetching operator data")
                            userRepository.fetchOperatorData(targetUserId).onSuccess { operator ->
                                operatorData = operator
                                android.util.Log.d("Profile", "Operator data fetched: ${operator.businessName}")
                            }.onFailure { e ->
                                android.util.Log.e("Profile", "Failed to load operator data: ${e.message}")
                                error = "Failed to load operator data: ${e.message}"
                            }
                        }
                    }.onFailure { e ->
                        android.util.Log.e("Profile", "Failed to load user data: ${e.message}")
                        error = "Failed to load user data: ${e.message}"
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Profile", "Error: ${e.message}")
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
                    // Profile Picture Section with upload functionality
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.Center)
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(profilePicUrl ?: R.drawable.default_profile)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { imagePickerLauncher.launch("image/*") }
                                )
                            }
                        }

                        // Edit button moved outside the profile picture
                        IconButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Profile Picture",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Profile Information
                    userData?.let { user ->
                        android.util.Log.d("Profile", "Displaying user data - Phone: ${user.phoneNumber}")
                        ProfileSection("Name", user.name)
                        ProfileSection("Email", user.email)
                        ProfileSection("Phone", user.phoneNumber ?: "Not provided")
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