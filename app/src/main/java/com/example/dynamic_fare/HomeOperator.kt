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
import android.util.Log
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorHomeScreen(navController: NavController, operatorId: String) {
    var selectedTab by remember { mutableStateOf(0) }
    val matatuList = remember { mutableStateListOf<Matatu>() }
    val fleetList = remember { mutableStateListOf<Fleet>() }
    var isLoadingMatatus by remember { mutableStateOf(true) }
    var isLoadingFleets by remember { mutableStateOf(true) }

    LaunchedEffect(operatorId) {
        Log.d("OperatorHomeScreen", "Loading data for operatorId: $operatorId")
        
        // Load matatus
        MatatuRepository.fetchMatatusForOperator(operatorId) { matatus ->
            matatuList.clear()
            matatuList.addAll(matatus)
            isLoadingMatatus = false
        }

        // Load fleets
        FleetRepository.fetchFleetsForOperator(operatorId) { fleets: List<Fleet> ->
            fleetList.clear()
            fleetList.addAll(fleets)
            isLoadingFleets = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operator Dashboard") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Routes.registrationRoute(operatorId))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = navController.currentDestination?.route ?: "",
                userId = operatorId
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                ) {
                    Text("Matatus")
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                ) {
                    Text("Fleet")
                }
            }

            if (isLoadingMatatus || isLoadingFleets) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> MatatuList(navController, matatuList, operatorId)
                    1 -> FleetList(navController, fleetList, operatorId)
                }
            }
        }
    }
}

@Composable
fun MatatuList(navController: NavController, matatuList: List<Matatu>, operatorId: String) {
    if (matatuList.isEmpty()) {
        EmptyStateMessage("No Matatus Available")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(matatuList) { matatu ->
                MatatuDetailItem(matatu, navController, operatorId)
            }
        }
    }
}

@Composable
fun FleetList(navController: NavController, fleetList: List<Fleet>, operatorId: String) {
    if (fleetList.isEmpty()) {
        EmptyStateMessage("No Fleets Available")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(fleetList) { fleet ->
                FleetDetailItem(fleet, navController, operatorId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatatuDetailItem(matatu: Matatu, navController: NavController, operatorId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                try {
                    // Use matatuId for navigation, fallback to registration number if needed
                    val matatuId = matatu.matatuId ?: matatu.registrationNumber
                    if (matatuId.isNotEmpty()) {
                        navController.navigate(Routes.fareTabbedRoute(matatuId))
                    } else {
                        Log.e("Navigation", "Invalid matatuId")
                    }
                } catch (e: Exception) {
                    Log.e("Navigation", "Error navigating to detail: ${e.message}")
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Registration: ${matatu.registrationNumber}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Route: ${matatu.routeStart} → ${matatu.routeEnd}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetDetailItem(fleet: Fleet, navController: NavController, operatorId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate(Routes.fleetRegistrationRoute(operatorId))
            },
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
