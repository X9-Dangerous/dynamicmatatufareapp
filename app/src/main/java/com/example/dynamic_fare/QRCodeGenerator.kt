package com.example.dynamic_fare.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QRScannerScreen(navController: NavController) {
    val context = LocalContext.current as Activity
    var scannedData by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scannedData = result.contents  // Retrieve scanned QR data
        } else {
            errorMessage = "Scan cancelled or failed."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scan QR Code to Pay", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            val options = ScanOptions()
            options.setPrompt("Scan the QR Code")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)
            scanLauncher.launch(options)
        }) {
            Text("Scan QR Code")
        }

        scannedData?.let {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Scanned Data: $it")
            Button(onClick = { navController.navigate("PaymentScreen/$it") }) {
                Text("Proceed to Pay")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(20.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
