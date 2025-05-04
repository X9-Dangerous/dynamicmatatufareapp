package com.example.dynamic_fare.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorHomeScreen(navController: NavController, operatorId: String) {
    Log.d("OperatorHomeScreen", "Passing operatorId from OperatorHomeScreen: $operatorId")
    var selectedTab by remember { mutableStateOf(0) }
    val matatuList = remember { mutableStateListOf<Matatu>() }
    val fleetList = remember { mutableStateListOf<Fleet>() }
    var isLoadingMatatus by remember { mutableStateOf(true) }
    var isLoadingFleets by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val matatuRepository = remember(context) { MatatuRepository(context) }
    val fleetRepository = remember(context) { FleetRepository(context) }

    LaunchedEffect(operatorId) {
        Log.d("OperatorHomeScreen", "Starting data load for operatorId: $operatorId")
        if (operatorId.isBlank()) {
            Log.e("OperatorHomeScreen", "Invalid operatorId: empty or blank")
            return@LaunchedEffect
        }
        // Load matatus
        Log.d("OperatorHomeScreen", "Fetching matatus for operatorId: $operatorId")
        coroutineScope.launch {
            val matatus = matatuRepository.fetchMatatusForOperator(operatorId)
            Log.d("OperatorHomeScreen", "Received "+matatus.size+" matatus for operatorId: $operatorId")
            matatuList.clear()
            matatuList.addAll(matatus)
            // Log all matatu IDs and registration numbers for debugging
            matatuList.forEach {
                Log.d("OperatorHomeScreen", "Matatu loaded: id=${it.matatuId}, reg=${it.registrationNumber}, routeStart=${it.routeStart}, routeEnd=${it.routeEnd}, mpesaOption=${it.mpesaOption}, sendMoneyPhone=${it.sendMoneyPhone}")
            }
            isLoadingMatatus = false
        }
        // Load fleets using the new SQLite-based approach
        Log.d("OperatorHomeScreen", "Fetching fleets for operatorId: $operatorId")
        coroutineScope.launch {
            val fleets = fleetRepository.fetchFleetsForOperator(operatorId)
            Log.d("OperatorHomeScreen", "Received ${fleets.size} fleets for operatorId: $operatorId")
            fleetList.clear()
            fleetList.addAll(fleets)
            // Log all fleet IDs and names for debugging
            fleetList.forEach {
                Log.d("OperatorHomeScreen", "Fleet loaded: id=${it.fleetId}, name=${it.fleetName}, operatorId=${it.operatorId}")
            }
            isLoadingFleets = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operator Dashboard") },
                actions = {
                    IconButton(onClick = {
                        // Navigate based on which tab is selected
                        if (selectedTab == 0) {
                            // Matatu tab - navigate to registration
                            navController.navigate(Routes.registrationRoute(operatorId))
                        } else {
                            // Fleet tab - navigate to fleet registration
                            navController.navigate(Routes.fleetRegistrationRoute(operatorId))
                        }
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
                    0 -> MatatuList(navController, matatuList, operatorId, coroutineScope, matatuRepository)
                    1 -> FleetList(navController, fleetList, operatorId, matatuList, coroutineScope, fleetRepository)
                }
            }
        }
    }
}

@Composable
fun MatatuList(navController: NavController, matatuList: List<Matatu>, operatorId: String, coroutineScope: CoroutineScope, matatuRepository: MatatuRepository) {
    if (matatuList.isEmpty()) {
        EmptyStateMessage("No Matatus Available")
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(matatuList) { matatu ->
                MatatuDetailItem(matatu, navController, operatorId, coroutineScope, matatuRepository)
            }
        }
    }
}

@Composable
fun FleetList(navController: NavController, fleetList: List<Fleet>, operatorId: String, matatuList: List<Matatu>, coroutineScope: CoroutineScope, fleetRepository: FleetRepository) {
    if (fleetList.isEmpty()) {
        EmptyStateMessage("No Fleets Available")
    } else {
        LazyColumn {
            items(fleetList) { fleet ->
                val matatuCount = matatuList.count { it.fleetname == fleet.fleetName }
                FleetDetailItem(fleet, navController, operatorId, matatuCount) { fleetToDelete ->
                    coroutineScope.launch {
                        val deleted = fleetRepository.deleteFleet(fleetToDelete.fleetId)
                        if (deleted) { /* update UI, show toast, etc. */ }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatatuDetailItem(matatu: Matatu, navController: NavController, operatorId: String, coroutineScope: CoroutineScope, matatuRepository: MatatuRepository) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (matatu.matatuId.isNotEmpty()) {
                    Log.d("MatatuDetailNav", "Navigating to details with matatuId=${matatu.matatuId}")
                    navController.navigate(Routes.fareTabbedRoute(matatu.matatuId))
                } else {
                    coroutineScope.launch {
                        val foundMatatuId = matatuRepository.getMatatuIdByRegistration(matatu.registrationNumber)
                        Log.d("MatatuDetailNav", "Looked up matatuId for reg=${matatu.registrationNumber}, found=$foundMatatuId")
                        if (!foundMatatuId.isNullOrEmpty()) {
                            navController.navigate(Routes.fareTabbedRoute(foundMatatuId))
                        } else {
                            Log.e("MatatuDetailNav", "Could not find matatuId for registration: ${matatu.registrationNumber}")
                        }
                    }
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Matatu details
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Registration: ${matatu.registrationNumber}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Route: ${matatu.routeStart} → ${matatu.routeEnd}", style = MaterialTheme.typography.bodyMedium)
            }
            
            var showDeleteDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            // Delete icon
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Matatu",
                    tint = Color.Red
                )
            }

            // Delete confirmation dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Matatu") },
                    text = { Text("Are you sure you want to delete this matatu?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                coroutineScope.launch {
                                    val matatuToDelete = matatu 
                                    matatuRepository.deleteMatatu(matatuToDelete)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Matatu deleted successfully",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetDetailItem(fleet: Fleet, navController: NavController, operatorId: String, matatuCount: Int, onDeleteFleet: (Fleet) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate(route = Routes.fleetDetailsRoute(fleet.fleetId))
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fleet: ${fleet.fleetName}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            if (matatuCount == 0) {
                var showDeleteDialog by remember { mutableStateOf(false) }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Fleet",
                        tint = Color.Red
                    )
                }
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Fleet") },
                        text = { Text("Are you sure you want to delete this fleet? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showDeleteDialog = false
                                onDeleteFleet(fleet)
                            }) {
                                Text("Delete", color = Color.Red)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}
