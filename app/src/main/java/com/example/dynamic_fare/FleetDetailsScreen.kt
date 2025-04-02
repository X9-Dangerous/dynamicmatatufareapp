package com.example.dynamic_fare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamic_fare.data.FleetRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetAndFareTabs(navController: NavController, fleetId: String) {
    var selectedTab by rememberSaveable { mutableStateOf(0) } // Ensure state persists across recompositions
    val tabTitles = listOf("Fleet Details", "Fare Details")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        println("🔄 Tab changed to: $index") // Debugging tab switch
                    },
                    text = { Text(title) }
                )
            }
        }

        // Debugging Output
        println("🚀 Currently Selected Tab: $selectedTab")

        when (selectedTab) {
            0 -> {
                println("🟢 Loading Fleet Details") // Debugging
                FleetDetailsScreen(navController, fleetId)
            }
            1 -> {
                println("🟢 Loading Fare Details") // Debugging
                FareDetailsScreen(navController, fleetId)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetDetailsScreen(navController: NavController, fleetId: String) {
    var fleetName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val fleetRef = remember { FirebaseDatabase.getInstance().reference.child("fleets").child(fleetId) }

    LaunchedEffect(fleetId) {
        fleetRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    fleetName = snapshot.child("fleetName").getValue(String::class.java) ?: ""
                }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Fleet Details") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Fleet ID: $fleetId", style = MaterialTheme.typography.bodyLarge)
                    Text("Fleet Name: $fleetName", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FareDetailsScreen(navController: NavController, fleetId: String) {
    var fareDetails by remember { mutableStateOf<Map<String, String>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val faresRef = remember { FirebaseDatabase.getInstance().reference.child("fares").child(fleetId) }

    LaunchedEffect(fleetId) {
        faresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    fareDetails = snapshot.children.associate { it.key!! to it.value.toString() }
                }
                isLoading = false
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Fare Details") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (fareDetails != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak Hours Fare: ${fareDetails?.get("peak_hours")}", style = MaterialTheme.typography.bodyLarge)
                    Text("Non-Peak Hours Fare: ${fareDetails?.get("non_peak_hours")}", style = MaterialTheme.typography.bodyLarge)
                    Text("Fare When Raining: ${fareDetails?.get("raining_fare")}", style = MaterialTheme.typography.bodyLarge)
                    Text("Fare When Not Raining: ${fareDetails?.get("non_raining_fare")}", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No fare details found. Please register fares.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigate("setFares") }) {
                        Text("Register Fares")
                    }
                }
            }
        }
    }
}

