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
    val notificationsEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(navController: NavController, userId: String) {
    var userSettings by remember { mutableStateOf(UserAccessibilitySettings(userId = userId)) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Keep track of the latest Firebase value
    var firebaseDisabledState by remember { mutableStateOf(false) }

    // Load user settings
    // Set up real-time listener for changes
    LaunchedEffect(userId) {
        val database = FirebaseDatabase.getInstance().getReference("userSettings")
        val userRef = database.child(userId)

        // Initial load
        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val isDisabled = snapshot.child("isDisabled").getValue(Boolean::class.java) ?: false
                val notificationsEnabled = snapshot.child("notificationsEnabled").getValue(Boolean::class.java) ?: true
                firebaseDisabledState = isDisabled
                userSettings = UserAccessibilitySettings(
                    userId = userId,
                    isDisabled = isDisabled,
                    notificationsEnabled = notificationsEnabled,
                    lastUpdated = snapshot.child("lastUpdated").getValue(Long::class.java) ?: System.currentTimeMillis()
                )
            } else {
                // Initialize settings if they don't exist
                val initialSettings = UserAccessibilitySettings(userId = userId)
                userRef.setValue(initialSettings)
                    .addOnSuccessListener {
                        userSettings = initialSettings
                        firebaseDisabledState = false
                    }
                    .addOnFailureListener { e ->
                        error = "Failed to initialize settings: ${e.message}"
                    }
            }
            isLoading = false
        }.addOnFailureListener { e ->
            error = "Error loading settings: ${e.message}"
            isLoading = false
        }

        // Set up real-time listener
        userRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (snapshot.exists()) {
                    val isDisabled = snapshot.child("isDisabled").getValue(Boolean::class.java) ?: false
                    firebaseDisabledState = isDisabled
                    userSettings = userSettings.copy(isDisabled = isDisabled)
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                android.util.Log.e("AccessibilitySettings", "Database error: ${error.message}")
            }
        })
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
                        checked = firebaseDisabledState,
                        onCheckedChange = { isDisabled ->
                            // Update local state immediately for UI responsiveness
                            firebaseDisabledState = isDisabled
                            
                            // Update in Database
                            FirebaseDatabase.getInstance()
                                .getReference("userSettings")
                                .child(userId)
                                .child("isDisabled")
                                .setValue(isDisabled)
                                .addOnFailureListener { e ->
                                    // Revert local state if update fails
                                    firebaseDisabledState = !isDisabled
                                    error = "Failed to update: ${e.message}"
                                }
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
