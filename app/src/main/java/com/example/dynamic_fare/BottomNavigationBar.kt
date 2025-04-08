package com.example.dynamic_fare.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.example.dynamic_fare.Routes

@Composable
fun BottomNavigationBar(navController: NavController, userId: String, currentRoute: String?) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Routes.operatorHomeRoute(userId)),
        BottomNavItem("Profile", Icons.Default.Person, Routes.profileRoute(userId))
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route)
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)
