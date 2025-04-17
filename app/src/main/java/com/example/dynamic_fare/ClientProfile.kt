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

    // Firebase references
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid

    val context = LocalContext.current

    // Cleanup function for Firebase listeners
    val valueEventListener = remember { mutableStateOf<ValueEventListener?>(null) }

    // Cleanup when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            // Remove the database listener
            valueEventListener.value?.let { listener ->
                userId?.let { uid ->
                    database.getReference("users/$uid").removeEventListener(listener)
                }
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && userId != null) {
            isUploadingImage = true

            // Reference to Firebase Storage
            val storageRef = storage.reference
            val imageRef = storageRef.child("profile_images/$userId.jpg")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Update the profile URL in the database
                        val userRef = database.getReference("users/$userId")
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

    // Fetch user data
    LaunchedEffect(userId) {
        if (userId != null) {
            val userRef = database.getReference("users/$userId")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        username = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        email = snapshot.child("email").getValue(String::class.java) ?: ""
                        phoneNumber = snapshot.child("phone").getValue(String::class.java) ?: ""
                        profilePicUrl = snapshot.child("profilePicUrl").getValue(String::class.java) ?: ""
                        isLoading = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    errorMessage = "Failed to load profile: ${error.message}"
                    isLoading = false
                }
            }
            
            // Store the listener reference for cleanup
            valueEventListener.value = listener
            userRef.addValueEventListener(listener)
        }
    }

    // Function to handle logout with proper cleanup
    val onLogout: () -> Unit = {
        // Remove the database listener
        valueEventListener.value?.let { listener ->
            userId?.let { uid ->
                database.getReference("users/$uid").removeEventListener(listener)
            }
        }
        
        // Sign out from Firebase
        auth.signOut()
        
        // Navigate to login and clear the back stack
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(Color.White)
    ) {
        // Profile Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture with loading state
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .clickable { imagePickerLauncher.launch("image/*") }
            ) {
                if (isUploadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    Image(
                        painter = if (profilePicUrl.isNotEmpty()) {
                            rememberAsyncImagePainter(profilePicUrl) // Load from URL
                        } else {
                            painterResource(id = R.drawable.ic_acccount) // Default profile image
                        },
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Username and Edit Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = username,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit profile picture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contact Information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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