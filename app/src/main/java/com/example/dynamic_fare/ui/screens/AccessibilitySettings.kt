package com.example.dynamic_fare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase

data class UserAccessibilitySettings(
    val userId: String = "",
    val isDisabled: Boolean = false,
    val notificationsEnabled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(navController: NavController, userId: String) {
    var userSettings by remember { mutableStateOf(UserAccessibilitySettings(userId = userId)) }
    var isLoading by remember { mutableStateOf(true) }

    // Load user settings
    LaunchedEffect(userId) {
        val database = FirebaseDatabase.getInstance().getReference("userSettings")
        database.child(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                userSettings = UserAccessibilitySettings(
                    userId = userId,
                    isDisabled = snapshot.child("isDisabled").getValue(Boolean::class.java) ?: false,
                    notificationsEnabled = snapshot.child("notificationsEnabled").getValue(Boolean::class.java) ?: true
                )
            }
            isLoading = false
        }
        .addOnFailureListener {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accessibility Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.Black)
            } else {
                // Disability Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Disability Status")
                    Switch(
                        checked = userSettings.isDisabled,
                        onCheckedChange = { isDisabled ->
                            userSettings = userSettings.copy(isDisabled = isDisabled)
                            // Update in Database
                            FirebaseDatabase.getInstance()
                                .getReference("userSettings")
                                .child(userId)
                                .setValue(userSettings)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Notifications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Notifications")
                    Switch(
                        checked = userSettings.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            userSettings = userSettings.copy(notificationsEnabled = enabled)
                            // Update in Database
                            FirebaseDatabase.getInstance()
                                .getReference("userSettings")
                                .child(userId)
                                .setValue(userSettings)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color.Black,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}
