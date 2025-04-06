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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import android.widget.Toast


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
    // State variables for user data
    var username by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var profilePicUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Get current user ID from Firebase Auth
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid

    val context = LocalContext.current

    // Function to handle image upload to Firebase Storage
    val handleImageUpload = { imageUri: Uri? ->
        if (imageUri != null && userId != null) {
            isUploadingImage = true

            // Reference to Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference
            val profileImageRef = storageRef.child("profile_images/$userId.jpg")

            // Upload the image
            val uploadTask = profileImageRef.putFile(imageUri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                profileImageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the download URL
                    val downloadUrl = task.result.toString()

                    // Update the profile URL in the database
                    val database = FirebaseDatabase.getInstance().reference
                    val userRef = database.child("users").child(userId)
                    userRef.child("profilePicUrl").setValue(downloadUrl)
                        .addOnSuccessListener {
                            // Update local state
                            profilePicUrl = downloadUrl
                            isUploadingImage = false
                            Toast.makeText(context, "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            isUploadingImage = false
                            errorMessage = "Failed to update profile picture: ${e.message}"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                } else {
                    isUploadingImage = false
                    errorMessage = "Failed to upload image: ${task.exception?.message}"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        handleImageUpload(uri)
    }

    // Function to handle logout
    val onLogout = {
        // Sign out from Firebase Auth
        auth.signOut()

        // Navigate back to login
        navController.navigate(Routes.LoginScreenContent) {
            popUpTo(0) { inclusive = true }
        }
    }

    // Effect to fetch user data when component is first loaded
    LaunchedEffect(userId) {
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Extract user details
                        username = snapshot.child("name").getValue(String::class.java) ?: "Client User"
                        email = snapshot.child("email").getValue(String::class.java) ?: currentUser.email ?: ""
                        phoneNumber = snapshot.child("phone").getValue(String::class.java) ?: ""
                        profilePicUrl = snapshot.child("profilePicUrl").getValue(String::class.java) ?: ""
                    } else {
                        // Use Firebase Auth data as fallback
                        username = currentUser?.displayName ?: "Client User"
                        email = currentUser?.email ?: ""
                    }
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ClientProfile", "Error fetching user data: ${error.message}")
                    errorMessage = "Could not load profile: ${error.message}"
                    isLoading = false
                }
            })
        } else {
            // No logged in user
            errorMessage = "No user logged in"
            isLoading = false
        }
    }

    ProfileScreen(
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        profilePicUrl = profilePicUrl,
        isLoading = isLoading || isUploadingImage,
        errorMessage = errorMessage,
        onLogout = onLogout,
        onProfilePictureClick = { imagePickerLauncher.launch("image/*") },
        navController = navController
    )
}

@Composable
fun ProfileScreen(
    username: String,
    email: String = "user@example.com",
    phoneNumber: String = "+254 712 345 678",
    profilePicUrl: String = "",
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onLogout: () -> Unit,
    onProfilePictureClick: () -> Unit = {},
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
            // Editable profile picture with overlay
            Box(modifier = Modifier
                .size(90.dp)
                .clickable { onProfilePictureClick() }
            ) {
                // Profile image
                Image(
                    painter = if (profilePicUrl.isNotEmpty()) {
                        rememberAsyncImagePainter(profilePicUrl) // Load from URL
                    } else {
                        painterResource(id = R.drawable.ic_acccount) // Default profile image
                    },
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Gray, shape = CircleShape)
                        .border(2.dp, Color.LightGray, CircleShape)
                )

                // Edit overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit profile picture",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

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
                        text = email,
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
                        text = phoneNumber.ifEmpty { "No phone number" },
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // Show any error messages
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Show loading indicator if needed
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading...", color = Color.Gray)
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(text = "Logout", color = Color.White)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        FooterWithIcons(navController)
    }
}