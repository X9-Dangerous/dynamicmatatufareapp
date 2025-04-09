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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.dynamic_fare.SetFaresActivity
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.models.MatatuFares

class FareTabbedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make content draw behind system bars
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

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

    Column(
        modifier = Modifier
            .statusBarsPadding() // Add padding for the status bar
    ) {
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        matatuDetails?.let { details ->
            // Vehicle Information Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Vehicle Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            label = "Registration Number",
                            value = details.registrationNumber,
                            isHighlighted = true
                        )
                    }
                }
            }

            // Route Information Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Route Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            label = "Route",
                            value = "${details.routeStart} → ${details.routeEnd}",
                            isHighlighted = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Stops",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = details.stops.joinToString(" → "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Payment Information Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Payment Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(
                            label = "M-Pesa Option",
                            value = details.mpesaOption,
                            isHighlighted = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        when (details.mpesaOption.lowercase()) {
                            "pochi la biashara" -> DetailRow(
                                label = "Pochi Number",
                                value = details.pochiNumber
                            )
                            "send money" -> DetailRow(
                                label = "Phone Number",
                                value = details.sendMoneyPhone
                            )
                            "till number" -> DetailRow(
                                label = "Till Number",
                                value = details.tillNumber
                            )
                            "paybill" -> {
                                DetailRow(
                                    label = "Paybill Number",
                                    value = details.paybillNumber
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DetailRow(
                                    label = "Account Number",
                                    value = details.accountNumber
                                )
                            }
                        }
                    }
                }
            }
        } ?: item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (error != null) {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading matatu details...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isHighlighted) {
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Fare Details",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading fare details...", 
                            style = MaterialTheme.typography.bodyLarge, 
                            color = Color.Black
                        )
                    }
                }
            }
        } else if (fareData == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No fare data found for this matatu.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
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
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Set Fares", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        } else {
            // Standard Fares Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Standard Fares",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        FareRow("Peak Hours", "Ksh ${fareData?.peakFare ?: ""}")
                        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        FareRow("Off-Peak Hours", "Ksh ${fareData?.nonPeakFare ?: ""}")
                    }
                }
            }
            
            // Rainy Weather Fares Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Rainy Weather Fares",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        FareRow("Peak Hours", "Ksh ${fareData?.rainyPeakFare ?: ""}")
                        Divider(color = Color(0xFFBBDEFB), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        FareRow("Off-Peak Hours", "Ksh ${fareData?.rainyNonPeakFare ?: ""}")
                    }
                }
            }
            
            // Disability Discount Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "♿ Disability Discount", 
                            style = MaterialTheme.typography.titleMedium, 
                            color = Color.Black
                        )
                        Text(
                            "${fareData?.disabilityDiscount ?: ""}%", 
                            style = MaterialTheme.typography.titleLarge, 
                            color = Color(0xFF558B2F)
                        )
                    }
                }
            }
            
            // Update Button
            item {
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
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("✏️ Update Fares", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun FareRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        Text(
            value, 
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
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