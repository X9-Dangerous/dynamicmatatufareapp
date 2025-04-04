package com.example.dynamic_fare.ui.screens

import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dynamic_fare.data.MatatuRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.dynamic_fare.Routes

fun saveQRCodeToStorage(context: Context, bitmap: Bitmap) {
    val filename = "matatu_qr_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QR_Codes")
    }

    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            Toast.makeText(context, "QR Code saved to gallery!", Toast.LENGTH_LONG).show()
        } ?: Toast.makeText(context, "Error saving QR code!", Toast.LENGTH_LONG).show()
    } ?: Toast.makeText(context, "Error saving QR code!", Toast.LENGTH_LONG).show()
}


@Composable
fun RegistrationScreen(navController: NavController, operatorId: String) {
    val context = LocalContext.current
    val intent = (context as? android.app.Activity)?.intent
    Log.d("RegistrationScreen", "operatorId received: $operatorId")
    val operatorId = intent?.getStringExtra("operatorId") ?: operatorId
    var regNumber by remember { mutableStateOf("") }
    var routeStart by remember { mutableStateOf("") }
    var routeEnd by remember { mutableStateOf("") }
    var stops by remember { mutableStateOf(mutableListOf<String>()) }
    var showMpesaPage by remember { mutableStateOf(false) }
    var mpesaType by remember { mutableStateOf("") }
    var pochiNumber by remember { mutableStateOf("") }
    var tillNumber by remember { mutableStateOf("") }
    var paybillNumber by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var sendMoneyPhone by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf("") }


    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                qrBitmap?.let { bitmap -> saveQRCodeToStorage(context, bitmap) }
            } else {
                Toast.makeText(context, "Permission denied! Unable to save QR code.", Toast.LENGTH_LONG).show()
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        if (!showMpesaPage) {
            Text("Matatu Registration", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                label = { Text("Registration Number") },
                value = regNumber,
                onValueChange = { regNumber = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = androidx.compose.ui.graphics.Color.Black)
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                label = { Text("Route Start") },
                value = routeStart,
                onValueChange = { routeStart = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = androidx.compose.ui.graphics.Color.Black)
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                label = { Text("Route End") },
                value = routeEnd,
                onValueChange = { routeEnd = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = androidx.compose.ui.graphics.Color.Black)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { stops.add("New Stop") }) {
                Text("Add Stop")
            }
            Spacer(modifier = Modifier.height(20.dp))

            stops.forEachIndexed { index, stop ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        label = { Text("Stop $index") },
                        value = stop,
                        onValueChange = { stops[index] = it },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(color = androidx.compose.ui.graphics.Color.Black)
                    )
                    IconButton(onClick = { stops.removeAt(index) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Stop")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // Validation will now happen after clicking "Next"
                    val isFormValid = regNumber.isNotBlank() && routeStart.isNotBlank() && routeEnd.isNotBlank()
                    if (isFormValid) {
                        showMpesaPage = true
                    } else {
                        errorMessage = "Please fill in all required fields."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical =8.dp, horizontal = 20.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                Text("Next", color = androidx.compose.ui.graphics.Color.Black)
            }

            // Error message display
            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Text("M-Pesa Payment Details", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            val mpesaOptions = listOf("Pochi la biashara", "Till Number", "Paybill", "Send Money")
            mpesaOptions.forEach { option ->
                Row(modifier = Modifier.fillMaxWidth().clickable { mpesaType = option }) {
                    RadioButton(selected = mpesaType == option, onClick = { mpesaType = option })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            when (mpesaType) {
                "Pochi la biashara" -> OutlinedTextField(label = { Text("Pochi Number", color = Color.Black) }, value = pochiNumber, onValueChange = { pochiNumber = it }, modifier = Modifier.fillMaxWidth())
                "Till Number" -> OutlinedTextField(label = { Text("Till Number", color = Color.Black) }, value = tillNumber, onValueChange = { tillNumber = it }, modifier = Modifier.fillMaxWidth())
                "Paybill" -> Column {
                    OutlinedTextField(label = { Text("Paybill Number", color = Color.Black) }, value = paybillNumber, onValueChange = { paybillNumber = it }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(label = { Text("Account Number", color = Color.Black) }, value = accountNumber, onValueChange = { accountNumber = it }, modifier = Modifier.fillMaxWidth())
                }
                "Send Money" -> OutlinedTextField(label = { Text("Phone Number", color = Color.Black) }, value = sendMoneyPhone, onValueChange = { sendMoneyPhone = it }, modifier = Modifier.fillMaxWidth())
            }
            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val qrData = "$regNumber|$routeStart|$routeEnd|$mpesaType|$pochiNumber|$paybillNumber|$accountNumber|$tillNumber|$sendMoneyPhone"
                        val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, 500, 500)
                        val barcodeEncoder = BarcodeEncoder()
                        qrBitmap = barcodeEncoder.createBitmap(bitMatrix)

                        // Save QR Code
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            saveQRCodeToStorage(context, qrBitmap!!)
                        } else {
                            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                saveQRCodeToStorage(context, qrBitmap!!)
                            } else {
                                permissionLauncher.launch(permission)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error generating QR code: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Text("Generate & Save QR Code")
            }

            Spacer(modifier = Modifier.height(20.dp))

// Display QR Code if generated
            qrBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Generated QR Code",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                coroutineScope.launch {
                    val stopsCopy = stops.toList()
                    MatatuRepository.registerMatatu(
                        matatuId = "",
                        operatorId = operatorId,
                        regNumber = regNumber,
                        routeStart = routeStart,
                        routeEnd = routeEnd,
                        stops = stops,
                        mpesaType = mpesaType,
                        pochiNumber = pochiNumber,
                        paybillNumber = paybillNumber,
                        accountNumber = accountNumber,
                        tillNumber = tillNumber,
                        sendMoneyPhone = sendMoneyPhone
                    ) { success ->
                        if (success) {
                            Toast.makeText(context, "Matatu Registered Successfully!", Toast.LENGTH_LONG).show()
                            navController.navigate(Routes.OperatorHome)
                        } else {
                            Toast.makeText(context, "Registration Failed!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }) {
                Text("Register Matatu")
            }
        }
    }
}
