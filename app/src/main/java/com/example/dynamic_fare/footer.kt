package com.example.dynamic_fare

import android.os.Bundle
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

@Composable
fun FooterWithIcons(navController: NavController) {
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
                        navController.navigate(Routes.MatatuEstimateScreen) {
                            popUpTo("home") { inclusive = true }
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
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            navController.navigate(Routes.profileRoute(userId)) {
                                launchSingleTop = true
                            }
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
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            navController.navigate(Routes.accessibilitySettingsRoute(userId)) {
                                launchSingleTop = true
                            }
                        }
                    }
            )
        }
    }
}

