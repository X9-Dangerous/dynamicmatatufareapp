package com.example.dynamic_fare.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dynamic_fare.SetFaresActivity
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.models.MatatuFares

class FareTabbedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matatuId = intent.getStringExtra("matatuId") ?: "" // Get matatuId from Intent

        setContent {
            val navController = rememberNavController() // Create NavController
            FareTabbedScreen(navController, matatuId)
        }
    }
}

@Composable
fun FareTabbedScreen(navController: NavController, matatuId: String) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Matatu Details", "Fare Details")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> MatatuDetailsScreen(navController, matatuId)
            1 -> FareDetailsScreen(matatuId)
        }
    }
}

@Composable
fun MatatuDetailsScreen(navController: NavController, matatuId: String) {
    var matatuDetails by remember { mutableStateOf<MatatuDetails?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(matatuId) {
        val database = FirebaseDatabase.getInstance().getReference("matatus")
        database.child(matatuId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                matatuDetails = snapshot.getValue(MatatuDetails::class.java)
            } else {
                error = "Matatu details not found"
            }
        }.addOnFailureListener { e ->
            error = "Error loading matatu details: ${e.message}"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Matatu Details",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        matatuDetails?.let { details ->
            // Registration Section
            item {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Registration Number",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                    Text(
                        text = details.registrationNumber,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                }
            }

            // Route Section
            item {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Route",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                    Text(
                        text = "${details.routeStart} → ${details.routeEnd}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
            }

            // Stops Section
            item {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = "Stops",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black
                    )
                    Text(
                        text = details.stops.joinToString(" → "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }

            // M-Pesa Payment Section
            item {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text("M-Pesa Payment Method", style = MaterialTheme.typography.titleSmall, color = Color.Black)

                    // Display the chosen method
                    Text("Selected Option: ${details.mpesaOption}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)

                    // Display the corresponding value
                    when (details.mpesaOption.lowercase()) {
                        "pochi la biashara" -> Text("Phone Number: ${details.pochiNumber}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                        "send money" -> Text("Phone Number: ${details.sendMoneyPhone}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                        "till number" -> Text("Till Number: ${details.tillNumber}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                        "paybill" -> {
                            Text("Paybill Number: ${details.paybillNumber}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                            Text("Account Number: ${details.accountNumber}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                        }
                        else -> Text("Unknown Option", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                    }
                }
            }

        } ?: item {
            Text(
                text = "Loading matatu details...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun FareDetailsScreen(matatuId: String) {
    val context = LocalContext.current
    var fareData by remember { mutableStateOf<MatatuFares?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(matatuId) {
        FareRepository.getFareDetails(matatuId) { fetchedFares ->
            fareData = fetchedFares
            isLoading = false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator()
            Text("Loading fare details...", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
        } else if (fareData == null) {
            Text("No fare data found for this matatu.", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(context, SetFaresActivity::class.java).apply {
                        putExtra("matatuId", matatuId)
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text("Set Fares")
            }
        } else {
            // Ensure displayed values are not null
            Text("Peak Fare: Ksh ${fareData?.peakFare ?: ""}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Text("Off-Peak Fare: Ksh ${fareData?.nonPeakFare ?: ""}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Text("Rainy Peak Fare: Ksh ${fareData?.rainyPeakFare ?: ""}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Text("Rainy Off-Peak Fare: Ksh ${fareData?.rainyNonPeakFare ?: ""}", style = MaterialTheme.typography.bodyLarge, color = Color.Black)
            Text("♿ Disability Discount: ${fareData?.disabilityDiscount ?: ""}%", style = MaterialTheme.typography.bodyLarge, color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val intent = Intent(context, SetFaresActivity::class.java)
                intent.putExtra("matatuId", matatuId)
                context.startActivity(intent)
            }) {
                Text("✏️ Update Fares")
            }
        }
    }
}

data class MatatuDetails(
    val matatuId: String = "",
    val registrationNumber: String = "",
    val routeStart: String = "",
    val routeEnd: String = "",
    val stops: List<String> = emptyList(),
    val mpesaOption: String = "",
    val pochiNumber: String = "",
    val sendMoneyPhone: String = "",
    val tillNumber: String = "",
    val paybillNumber: String = "",
    val accountNumber: String = "",
    val operatorId: String = ""
)
