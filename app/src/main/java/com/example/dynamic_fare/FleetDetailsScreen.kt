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
import com.example.dynamic_fare.data.MatatuRepository
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetDetailsScreen(navController: NavController, fleetId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var fleetName by remember { mutableStateOf("") }
    var matatuCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(fleetId) {
        coroutineScope.launch {
            val fleetRepo = FleetRepository(context)
            val matatuRepo = MatatuRepository(context)
            val fleet = fleetRepo.fetchFleetDetails(fleetId)
            fleetName = fleet?.fleetName ?: ""
            val allMatatus = matatuRepo.getAllMatatus()
            matatuCount = allMatatus.count { it.fleetname == fleetName }
            isLoading = false
        }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Fleet ID: $fleetId", style = MaterialTheme.typography.bodyLarge)
                    Text("Fleet Name: $fleetName", style = MaterialTheme.typography.bodyMedium)
                    Text("Number of Matatus: $matatuCount", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
