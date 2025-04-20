package com.example.dynamic_fare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamic_fare.R
import com.example.dynamic_fare.ui.theme.DynamicMatauFareAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun FooterWithIcons(
    navController: NavController,
    userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
) {
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
                        Log.d("FooterWithIcons", "Navigating to home with userId: $userId")
                        navController.navigate(Routes.homeRoute(userId)) {
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
                        // Get current user ID from Firebase Auth
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            Log.d("FooterWithIcons", "Navigating to notifications with userId: $userId")
                            navController.navigate(Routes.notificationsRoute(userId)) {
                                launchSingleTop = true
                            }
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
                        Log.d("FooterWithIcons", "Navigating to client profile with userId: $userId")
                        navController.navigate(Routes.clientProfileRoute(userId)) {
                            launchSingleTop = true
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
                        try {
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            if (currentUserId != null) {
                                // First ensure settings exist
                                FirebaseDatabase.getInstance()
                                    .getReference("userSettings")
                                    .child(currentUserId)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        if (!snapshot.exists()) {
                                            // Initialize settings if they don't exist
                                            val initialSettings = mapOf(
                                                "userId" to currentUserId,
                                                "isDisabled" to false,
                                                "notificationsEnabled" to true,
                                                "lastUpdated" to System.currentTimeMillis()
                                            )
                                            FirebaseDatabase.getInstance()
                                                .getReference("userSettings")
                                                .child(currentUserId)
                                                .setValue(initialSettings)
                                        }
                                        Log.d("FooterWithIcons", "Navigating to accessibility settings with userId: $currentUserId")
                                        navController.navigate(Routes.accessibilitySettingsRoute(currentUserId)) {
                                            launchSingleTop = true
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Footer", "Error checking settings: ${e.message}")
                                    }
                            } else {
                                Log.e("Footer", "No user logged in")
                            }
                        } catch (e: Exception) {
                            Log.e("Footer", "Error navigating to settings: ${e.message}")
                        }
                    }
            )
        }
    }
}
