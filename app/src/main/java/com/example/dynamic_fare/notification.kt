package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.compose.rememberNavController


@OptIn(ExperimentalMaterial3Api::class) // Optional: Can be removed if TopAppBar is stable in your version
@Composable
fun NotificationsScreen(navController: NavController) {
    val notifications = remember {
        mutableStateListOf(
            "Morning fares have increased by Ksh 10",
            "Rainy weather detected, fares may rise",
            "New feature: Track matatu locations in real-time",
            "Your last trip cost Ksh 50"
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // Material3 version of TopAppBar
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            items(notifications) { notification ->
                NotificationItem(notification)
            }
        }
    }
}

@Composable
fun NotificationItem(notification: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Text(
            text = notification,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen(navController = rememberNavController())
}
