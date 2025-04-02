
package com.example.dynamic_fare.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.Routes
import com.example.dynamic_fare.data.MatatuRepository
import com.example.dynamic_fare.data.FleetRepository
import com.example.dynamic_fare.models.Fleet
import com.example.dynamic_fare.models.Matatu
import com.example.dynamic_fare.ui.components.BottomNavigationBar

@Composable
fun OperatorHomeScreen(navController: NavController, operatorId: String) {
    var selectedTab by remember { mutableStateOf(0) }
    val matatuList = remember { mutableStateListOf<Matatu>() }
    val fleetList = remember { mutableStateListOf<Fleet>() }
    var isLoadingMatatus by remember { mutableStateOf(true) }
    var isLoadingFleets by remember { mutableStateOf(true) }
    val currentRoute = navController.currentDestination?.route ?: ""

    // Fetch data before the screen is rendered
    LaunchedEffect(Unit) {
        MatatuRepository.fetchMatatusForOperator(operatorId) { matatus ->
            matatuList.clear()
            matatuList.addAll(matatus)
            isLoadingMatatus = false
        }

        FleetRepository.fetchFleetsForOperator(operatorId) { fleets ->
            fleetList.clear()
            fleetList.addAll(fleets)
            isLoadingFleets = false
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, currentRoute, operatorId) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val route = if (selectedTab == 0) Routes.RegistrationScreen else Routes.FleetRegistrationScreen
                    navController.navigate(route)
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Single Matatu")
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Fleet")
                }
            }

            if (isLoadingMatatus || isLoadingFleets) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (selectedTab == 0) {
                    MatatuList(navController, matatuList)
                } else {
                    FleetList(navController, fleetList)
                }
            }
        }
    }
}

@Composable
fun MatatuList(navController: NavController, matatuList: MutableList<Matatu>) {
    if (matatuList.isEmpty()) {
        EmptyStateMessage("No Matatus Available")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(matatuList) { matatu ->
                MatatuDetailItem(
                    matatu = matatu,
                    onClick = { navController.navigate(Routes.FareTabbedScreen) },
                )
            }
        }
    }
}

@Composable
fun MatatuDetailItem(matatu: Matatu, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }, // Fixed: Added onClick parameter
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Registration: ${matatu.registrationNumber}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Route: ${matatu.routeStart} → ${matatu.routeEnd}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun FleetList(navController: NavController, fleetList: List<Fleet>) {
    if (fleetList.isEmpty()) {
        EmptyStateMessage("No Fleets Available")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(fleetList) { fleet ->
                FleetDetailItem(fleet = fleet) {
                    navController.navigate(Routes.FareTabbedScreen)
                }
            }
        }
    }
}

@Composable
fun FleetDetailItem(fleet: Fleet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = "Fleet: ${fleet.fleetName}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

