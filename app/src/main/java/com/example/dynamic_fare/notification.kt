package com.example.dynamic_fare

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.FirebaseDatabase

data class PaymentHistoryItem(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val route: String = "",
    val timestamp: Long = 0,
    val status: String = "",
    val startLocation: String = "",
    val endLocation: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(navController: NavController, userId: String) {
    var payments by remember { mutableStateOf<List<PaymentHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        val database = FirebaseDatabase.getInstance().getReference("payments")
        database.orderByChild("userId").equalTo(userId).get()
            .addOnSuccessListener { snapshot ->
                val paymentsList = mutableListOf<PaymentHistoryItem>()
                snapshot.children.forEach { child ->
                    val payment = PaymentHistoryItem(
                        id = child.key ?: "",
                        userId = child.child("userId").getValue(String::class.java) ?: "",
                        amount = child.child("amount").getValue(Double::class.java) ?: 0.0,
                        route = child.child("route").getValue(String::class.java) ?: "",
                        timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L,
                        status = child.child("status").getValue(String::class.java) ?: "completed",
                        startLocation = child.child("startLocation").getValue(String::class.java) ?: "",
                        endLocation = child.child("endLocation").getValue(String::class.java) ?: ""
                    )
                    paymentsList.add(payment)
                }
                payments = paymentsList.sortedByDescending { it.timestamp }
                isLoading = false
            }
            .addOnFailureListener { e ->
                error = e.message
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                payments.isEmpty() -> {
                    Text(
                        text = "No payment history",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn {
                        items(payments) { payment ->
                            PaymentHistoryCard(payment)
                        }
                    }
                }
            }
        }
    }
                }


@Composable
fun PaymentHistoryCard(payment: PaymentHistoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "KES ${String.format("%.2f", payment.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = payment.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when(payment.status) {
                        "completed" -> Color.Green
                        "failed" -> Color.Red
                        else -> Color.Gray
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${payment.startLocation} → ${payment.endLocation}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Route: ${payment.route}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date(payment.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentHistoryScreenPreview() {
    PaymentHistoryScreen(
        navController = rememberNavController(),
        userId = "preview_user_id"
    )
}
