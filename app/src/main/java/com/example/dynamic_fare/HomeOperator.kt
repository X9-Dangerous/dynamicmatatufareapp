package com.example.dynamic_fare

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import com.example.dynamic_fare.data.MatatuRepository
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.utils.generateQRCodeBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save


@Composable
fun DisplayInfoScreen(navController: NavController, operatorId: String) {
    var regNumber by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var uniqueCode by remember { mutableStateOf("") }

    LaunchedEffect(operatorId) {
        MatatuRepository.fetchMatatuData(operatorId) { fetchedRegNumber, fetchedRoute, fetchedUniqueCode ->
            regNumber = fetchedRegNumber
            route = fetchedRoute
            uniqueCode = fetchedUniqueCode
            qrBitmap = generateQRCodeBitmap(fetchedUniqueCode)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Matatu Info", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Registration Number: $regNumber", fontSize = 18.sp)
        Text("Route: $route", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(20.dp))

        qrBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(150.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button - Generates & Saves QR Code
        Button(
            onClick = {
                MatatuRepository.saveMatatuDetails(operatorId, regNumber, route) { newUniqueCode, newBitmap ->
                    uniqueCode = newUniqueCode
                    qrBitmap = newBitmap
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save & Generate QR")
        }
    }
}
