package com.example.dynamic_fare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamic_fare.ui.ChooseFleetDialog
import com.example.dynamic_fare.data.MatatuRepository
import com.example.dynamic_fare.ui.components.BottomNavigationBar

@Composable
fun OperatorHomeScreen(navController: NavController, operatorId: String) {
    var matatuList by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(operatorId) {
        MatatuRepository.fetchMatatusForOperator(operatorId) { fetchedMatatus ->
            matatuList = fetchedMatatus
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Matatu/Fleet")
            }
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn {
                items(matatuList) { matatu ->
                    Text(text = matatu)
                }
            }
        }
    }

    if (showAddDialog) {
        ChooseFleetDialog(
            onDismiss = { showAddDialog = false },
            onSelection = { isFleet ->
                val route = if (isFleet) "fleetRegistration" else "matatuRegistration"
                navController.navigate("$route/$operatorId")
            }
        )
    }
}
