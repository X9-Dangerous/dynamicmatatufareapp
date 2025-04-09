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

import com.example.dynamic_fare.models.Payment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun PaymentDetailsDialog(
    payment: Payment,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment Details") },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Amount: KES ${String.format("%.2f", payment.amount)}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Status: ${payment.status}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Route: ${payment.route}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("From: ${payment.startLocation}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("To: ${payment.endLocation}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Matatu: ${payment.matatuRegistration}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("M-Pesa Receipt: ${payment.mpesaReceiptNumber}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Paid via: ${payment.paymentMethod}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Phone: ${payment.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Date: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(payment.timestamp))}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(navController: NavController, userId: String) {
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedPayment by remember { mutableStateOf<Payment?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        val database = FirebaseDatabase.getInstance().getReference("payments")
        database.orderByChild("userId").equalTo(userId).get()
            .addOnSuccessListener { snapshot ->
                val paymentsList = mutableListOf<Payment>()
                snapshot.children.forEach { child ->
                    val payment = Payment(
                        id = child.key ?: "",
                        userId = child.child("userId").getValue(String::class.java) ?: "",
                        amount = child.child("amount").getValue(Double::class.java) ?: 0.0,
                        route = child.child("route").getValue(String::class.java) ?: "",
                        timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L,
                        status = child.child("status").getValue(String::class.java) ?: "completed",
                        startLocation = child.child("startLocation").getValue(String::class.java) ?: "",
                        endLocation = child.child("endLocation").getValue(String::class.java) ?: "",
                        matatuRegistration = child.child("matatuRegistration").getValue(String::class.java) ?: "",
                        mpesaReceiptNumber = child.child("mpesaReceiptNumber").getValue(String::class.java) ?: "",
                        paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: "",
                        phoneNumber = child.child("phoneNumber").getValue(String::class.java) ?: ""
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
                            PaymentHistoryCard(
                                payment = payment,
                                onClick = { selectedPayment = payment }
                            )
                        }
                    }
                }
            }
        }
    }

    // Show payment details dialog
    selectedPayment?.let { payment ->
        PaymentDetailsDialog(
            payment = payment,
            onDismiss = { selectedPayment = null },
            onDelete = { showDeleteConfirmation = true }
        )
    }

    // Show delete confirmation dialog
    if (showDeleteConfirmation && selectedPayment != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Payment", style = MaterialTheme.typography.headlineSmall) },
            text = { 
                Text(
                    "Are you sure you want to delete this payment record?", 
                    style = MaterialTheme.typography.bodyLarge
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Delete from Firebase
                        val database = FirebaseDatabase.getInstance().getReference("payments")
                        database.child(selectedPayment!!.id).removeValue()
                            .addOnSuccessListener {
                                // Remove from local list
                                payments = payments.filter { it.id != selectedPayment!!.id }
                                showDeleteConfirmation = false
                                selectedPayment = null
                            }
                            .addOnFailureListener { e ->
                                // Show error message
                                error = "Failed to delete payment: ${e.message}"
                            }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
            }



@Composable
fun PaymentHistoryCard(payment: Payment, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
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
