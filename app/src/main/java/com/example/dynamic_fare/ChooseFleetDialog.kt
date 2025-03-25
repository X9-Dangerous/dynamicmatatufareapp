package com.example.dynamic_fare.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ChooseFleetDialog(onDismiss: () -> Unit, onSelection: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Choose Registration Type", fontWeight = FontWeight.Bold) },
        text = { Text("Would you like to register a single matatu or a fleet?", color = Color.Black) },
        confirmButton = {
            Button(onClick = { onSelection(false) }) { Text("Single Matatu", color = Color.Black) }
            Button(onClick = { onSelection(true) }) { Text("Fleet", color = Color.Black) }
        }
    )
}
