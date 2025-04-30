package com.example.dynamic_fare

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dynamic_fare.R
import com.example.dynamic_fare.auth.SqliteUserRepository
import com.example.dynamic_fare.datastore.UserSessionDataStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                username = "John Doe",
                profilePicUrl = "https://example.com/profile.jpg",
                onLogout = { /* Handle logout */ }
            )
        }
    }

    private fun ProfileScreen(username: String, profilePicUrl: String, onLogout: () -> Unit) {
        // Implementation remains the same
    }
}

@Composable
fun ClientProfileScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val userRepository = remember { SqliteUserRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var userData by remember { mutableStateOf<com.example.dynamic_fare.auth.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch user data when screen loads
    LaunchedEffect(userId) {
        Log.d("ClientProfileScreen", "Screen launched with userId: '$userId'")
        
        if (userId.isNotEmpty()) {
            coroutineScope.launch {
                try {
                    Log.d("ClientProfileScreen", "Attempting to fetch user data from SQLite")
                    val user = userRepository.getUserByEmail(userId)
                    if (user != null) {
                        userData = user
                        Log.d("ClientProfileScreen", "Successfully fetched user data: ${user.name}, ${user.email}")
                    } else {
                        errorMessage = "User not found in database."
                        Log.e("ClientProfileScreen", "User not found in database for email: $userId")
                    }
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                    Log.e("ClientProfileScreen", "Error fetching user data: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
            errorMessage = "No user ID provided."
            Log.e("ClientProfileScreen", "No user ID provided - userId is empty")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Client Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            userData != null -> {
                // Profile Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        ProfileField(
                            label = "Name",
                            value = userData!!.name,
                            icon = Icons.Default.Person
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField(
                            label = "Email",
                            value = userData!!.email,
                            icon = Icons.Default.Email
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField(
                            label = "Phone",
                            value = userData!!.phone.ifEmpty { "Not provided" },
                            icon = Icons.Default.Phone
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileField(
                            label = "Role",
                            value = userData!!.role,
                            icon = Icons.Default.Badge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Button
        Button(
            onClick = {
                coroutineScope.launch {
                    UserSessionDataStore.clearUserEmail(context)
                    navController.navigate("login") {
                        popUpTo("clientHome") { inclusive = true }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Logout", color = Color.White)
        }

        // Add FooterWithIcons
        FooterWithIcons(navController, userId)
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}