package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamic_fare.R
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.example.dynamic_fare.auth.SqliteUserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

@Composable
fun FooterWithIcons(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    val userRepository = remember { SqliteUserRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    var currentUserEmail by remember { mutableStateOf<String?>(null) }

    // Fetch current user email when footer loads
    LaunchedEffect(userId) {
        coroutineScope.launch {
            try {
                if (userId.isNotEmpty()) {
                    val user = userRepository.getUserByEmail(userId)
                    if (user != null) {
                        currentUserEmail = user.email
                        Log.d("FooterWithIcons", "Found current user email: $currentUserEmail")
                    } else {
                        Log.e("FooterWithIcons", "No user found in SQLite database for email: $userId")
                    }
                } else {
                    Log.e("FooterWithIcons", "No userId provided to FooterWithIcons")
                }
            } catch (e: Exception) {
                Log.e("FooterWithIcons", "Error fetching user: ${e.message}")
            }
        }
    }

    Log.d("FooterWithIcons", "Footer loaded with userId: $userId")
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f)) // Push footer to the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_home),
                contentDescription = "Home",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        Log.d("FooterWithIcons", "Navigating to home")
                        navController.navigate(Routes.homeRoute(currentUserEmail ?: "")) {
                            popUpTo(Routes.HomeScreen) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_alerts),
                contentDescription = "Notifications",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        if (currentUserEmail != null) {
                            Log.d("FooterWithIcons", "Navigating to notifications with userId: $currentUserEmail")
                            navController.navigate(Routes.notificationsRoute(currentUserEmail!!)) {
                                launchSingleTop = true
                            }
                        } else {
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                        }
                    }
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_acccount),
                contentDescription = "Profile",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        if (currentUserEmail != null) {
                            Log.d("FooterWithIcons", "Navigating to client profile with userId: $currentUserEmail")
                            navController.navigate(Routes.clientProfileRoute(currentUserEmail!!)) {
                                launchSingleTop = true
                            }
                        } else {
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                        }
                    }
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = "Settings",
                tint = Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        if (currentUserEmail != null) {
                            Log.d("FooterWithIcons", "Navigating to accessibility settings with userId: $currentUserEmail")
                            navController.navigate(Routes.accessibilitySettingsRoute(currentUserEmail!!)) {
                                launchSingleTop = true
                            }
                        } else {
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                        }
                    }
            )
        }
    }
}
