package com.example.dynamic_fare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dynamic_fare.models.Matatu

@Composable
fun MatatuDetailItem(matatu: Matatu, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Matatu ID: ${matatu.matatuId}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = "Registration: ${matatu.registrationNumber}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Route: ${matatu.routeStart} → ${matatu.routeEnd}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
