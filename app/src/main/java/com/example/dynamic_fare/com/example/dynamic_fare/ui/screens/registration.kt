package com.example.dynamic_fare.ui.screens

import android.graphics.Bitmap
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dynamic_fare.data.MatatuRepository
import com.example.dynamic_fare.data.FleetRepository
import com.example.dynamic_fare.models.Fleet
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.content.Context
import android.provider.MediaStore
import android.content.ContentValues
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.dynamic_fare.Routes
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.dynamic_fare.models.QrCode
import com.example.dynamic_fare.AppDatabase
import java.util.UUID
import java.io.ByteArrayOutputStream

// Data class to hold GTFS stop suggestion
data class GtfsStopSuggestion(
    val stopId: String,
    val stopName: String,
    val stopCode: String,
    val latitude: Double,
    val longitude: Double
)

// Function to get GTFS stop suggestions based on input text
fun getGtfsStopSuggestions(input: String, context: Context): List<GtfsStopSuggestion> {
    if (input.isEmpty() || input.length < 2) return emptyList()

    Log.d("GTFS_DEBUG", "Searching for suggestions with input: '$input'")

    try {
        // Get all GTFS stops from the stops1.txt file
        val gtfsStops = getGtfsStopsFromFile(context)
        Log.d("GTFS_DEBUG", "Got ${gtfsStops.size} total stops from file")

        // Filter the stops based on user input
        val filteredStops = gtfsStops.filter {
            it.stopName.contains(input, ignoreCase = true) ||
                    it.stopCode.contains(input, ignoreCase = true)
        }.take(15) // Limit results to prevent overwhelming the UI

        Log.d("GTFS_DEBUG", "Filtered to ${filteredStops.size} matching stops")
        if (filteredStops.isNotEmpty()) {
            Log.d("GTFS_DEBUG", "First match: ${filteredStops.first().stopName}")
        }

        return filteredStops
    } catch (e: Exception) {
        Log.e("GTFS_DEBUG", "Error getting stop suggestions: ${e.message}", e)
        // Fallback to sample data in case of error
        Log.d("GTFS_DEBUG", "Using fallback sample data")
        val fallbackStops = getSampleStops().filter {
            it.stopName.contains(input, ignoreCase = true) ||
                    it.stopCode.contains(input, ignoreCase = true)
        }
        Log.d("GTFS_DEBUG", "Found ${fallbackStops.size} fallback matches")
        return fallbackStops
    }

}

// Helper function to read GTFS stops from the stops1.txt file
private fun getGtfsStopsFromFile(context: Context): List<GtfsStopSuggestion> {
    val stops = mutableListOf<GtfsStopSuggestion>()

    try {
        // Try opening from gtfs subfolder first
        Log.d("GTFS_DEBUG", "Attempting to open gtfs/stops1.txt")
        context.assets.open("gtfs/stops1.txt").bufferedReader().use { reader ->
            // Read all lines
            val lines = reader.readLines()

            Log.d("GTFS_DEBUG", "Found ${lines.size} lines in stops1.txt")

            // Skip header and process all data lines
            if (lines.isNotEmpty()) {
                lines.drop(1).forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 5) {
                        try {
                            val stopId = parts[0].trim()
                            val stopCode = parts[1].trim().ifEmpty { stopId } // Use stop_id if stop_code is empty
                            val stopName = parts[2].trim()
                            val stopLat = parts[3].trim().toDoubleOrNull() ?: 0.0
                            val stopLon = parts[4].trim().toDoubleOrNull() ?: 0.0

                            // Only add stops with valid names and coordinates
                            if (stopName.isNotEmpty() && stopLat != 0.0 && stopLon != 0.0) {
                                stops.add(
                                    GtfsStopSuggestion(
                                        stopId = stopId,
                                        stopName = stopName,
                                        stopCode = stopCode,
                                        latitude = stopLat,
                                        longitude = stopLon
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("GTFS", "Error parsing stop line: $line", e)
                            // Continue to next line on error
                        }
                    }
                }
            }
        }

        Log.d("GTFS", "Loaded ${stops.size} stops from GTFS data")
    } catch (e: Exception) {
        Log.e("GTFS", "Error reading stops.txt file", e)
    }

    return stops
}

// Fallback sample stops in case file reading fails
private fun getSampleStops(): List<GtfsStopSuggestion> {
    return listOf(
        GtfsStopSuggestion("1", "Nairobi Central Station", "NCS1", -1.286389, 36.817223),
        GtfsStopSuggestion("2", "Westlands Terminal", "WT2", -1.267788, 36.803458),
        GtfsStopSuggestion("3", "Kayole Stop", "KYL3", -1.279465, 36.908329),
        GtfsStopSuggestion("4", "Kibera Station", "KBR4", -1.311866, 36.780031),
        GtfsStopSuggestion("5", "Karen Terminal", "KRN5", -1.319363, 36.706510),
        GtfsStopSuggestion("6", "Rongai Stop", "RNG6", -1.396967, 36.754679),
        GtfsStopSuggestion("7", "Ngong Road Station", "NGR7", -1.299040, 36.764802),
        GtfsStopSuggestion("8", "CBD Main Stop", "CBD8", -1.284756, 36.824061),
        GtfsStopSuggestion("9", "Eastleigh Terminal", "EST9", -1.274872, 36.851430),
        GtfsStopSuggestion("10", "Thika Road Mall", "TRM10", -1.219692, 36.888138)
    )
}

// GTFS input field with auto-suggestions
@Composable
fun GtfsInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSuggestionSelected: (GtfsStopSuggestion) -> Unit,
    label: String,
    suggestions: List<GtfsStopSuggestion>,
    showSuggestions: Boolean,
    onDismissSuggestions: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, color = Color.Black) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 4.dp),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )

            // Dropdown suggestions
            if (showSuggestions && suggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp) // Maximum height for the dropdown
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSuggestionSelected(suggestion)
                                    onDismissSuggestions()
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = suggestion.stopName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Stop #${suggestion.stopCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationScreen(navController: NavController, operatorId: String) {
    Log.d("RegistrationScreen", "Received operatorId in RegistrationScreen: $operatorId")
    var regNumber by remember { mutableStateOf("") }
    var routeStart by remember { mutableStateOf("") }
    var routeEnd by remember { mutableStateOf("") }
    var stops by remember { mutableStateOf(mutableListOf<String>()) }
    var currentStop by remember { mutableStateOf("") }
    var mpesaType by remember { mutableStateOf("") }
    var pochiNumber by remember { mutableStateOf("") }
    var paybillNumber by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var tillNumber by remember { mutableStateOf("") }
    var sendMoneyPhone by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fleets by remember { mutableStateOf<List<Fleet>>(emptyList()) }
    var selectedFleet by remember { mutableStateOf<Fleet?>(null) }
    var showFleetDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showMpesaPage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val matatuRepository = remember(context) { MatatuRepository(context) }
    val fleetRepository = remember(context) { FleetRepository(context) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Storage permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to check and request storage permission
    fun checkAndRequestStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Android 10 and above don't need storage permission for saving to gallery
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    true
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    false
                }
            }
        }
    }

    // Function to save QR code to gallery
    fun saveQRCodeToGallery(qrBitmap: Bitmap, registrationNumber: String): Boolean {
        if (!checkAndRequestStoragePermission()) {
            return false
        }

        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "matatu_qr_${registrationNumber}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
            }

            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    Toast.makeText(context, "QR Code saved to gallery", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save QR Code to gallery: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    // Fetch fleets for this operator
    LaunchedEffect(operatorId) {
        coroutineScope.launch {
            isLoading = true
            try {
                val fetchedFleets = fleetRepository.fetchFleetsForOperator(operatorId)
                fleets = fetchedFleets
                if (fleets.isNotEmpty()) {
                    showFleetDialog = true
                }
            } catch (e: Exception) {
                Log.e("RegistrationScreen", "Error fetching fleets", e)
            } finally {
                isLoading = false
            }
        }
    }

    // GTFS suggestion states
    var startingSuggestions by remember { mutableStateOf<List<GtfsStopSuggestion>>(emptyList()) }
    var destinationSuggestions by remember { mutableStateOf<List<GtfsStopSuggestion>>(emptyList()) }
    var showStartingSuggestions by remember { mutableStateOf(false) }
    var showDestinationSuggestions by remember { mutableStateOf(false) }

    // Coordinates for start and end points from GTFS data
    var startLocationCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var endLocationCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Load initial GTFS data when the screen is first shown
    LaunchedEffect(Unit) {
        try {
            Log.d("GTFS", "Initial loading of GTFS data")
            // Pre-load GTFS data to ensure it's available
            val initialStops = getGtfsStopsFromFile(context)
            Log.d("GTFS", "Preloaded ${initialStops.size} GTFS stops")
        } catch (e: Exception) {
            Log.e("GTFS", "Error preloading GTFS data: ${e.message}", e)
        }
    }

    // Effect to update suggestions when input changes
    LaunchedEffect(routeStart) {
        if (routeStart.length >= 2) {
            startingSuggestions = getGtfsStopSuggestions(routeStart, context)
            Log.d("GTFS", "Found ${startingSuggestions.size} start suggestions for '$routeStart'")
            showStartingSuggestions = startingSuggestions.isNotEmpty()
        } else {
            showStartingSuggestions = false
        }
    }

    LaunchedEffect(routeEnd) {
        if (routeEnd.length >= 2) {
            destinationSuggestions = getGtfsStopSuggestions(routeEnd, context)
            Log.d("GTFS", "Found ${destinationSuggestions.size} end suggestions for '$routeEnd'")
            showDestinationSuggestions = destinationSuggestions.isNotEmpty()
        } else {
            showDestinationSuggestions = false
        }
    }

    // Helper function to convert Bitmap to ByteArray
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(20.dp)) {
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

            // Route Start with GTFS autosuggestions
            GtfsInputField(
                value = routeStart,
                onValueChange = {
                    routeStart = it
                    startLocationCoordinates = null // Reset coordinates when manually typing
                },
                onSuggestionSelected = { suggestion ->
                    routeStart = suggestion.stopName
                    startLocationCoordinates = Pair(suggestion.latitude, suggestion.longitude)
                    Log.d("GTFS", "Selected start suggestion: ${suggestion.stopName} at ${suggestion.latitude},${suggestion.longitude}")
                },
                label = "Route Start",
                suggestions = startingSuggestions,
                showSuggestions = showStartingSuggestions,
                onDismissSuggestions = { showStartingSuggestions = false },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Route End with GTFS autosuggestions
            GtfsInputField(
                value = routeEnd,
                onValueChange = {
                    routeEnd = it
                    endLocationCoordinates = null // Reset coordinates when manually typing
                },
                onSuggestionSelected = { suggestion ->
                    routeEnd = suggestion.stopName
                    endLocationCoordinates = Pair(suggestion.latitude, suggestion.longitude)
                    Log.d("GTFS", "Selected end suggestion: ${suggestion.stopName} at ${suggestion.latitude},${suggestion.longitude}")
                },
                label = "Route End",
                suggestions = destinationSuggestions,
                showSuggestions = showDestinationSuggestions,
                onDismissSuggestions = { showDestinationSuggestions = false },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
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
                    val stopsCopy = stops.toList()
                    val registeredMatatuId = UUID.randomUUID().toString()
                    
                    // Log before registration attempt
                    Log.d("MatatuRegistration", "Attempting to register matatu with ID: $registeredMatatuId, reg: $regNumber")
                    
                    val success = matatuRepository.registerMatatu(
                        matatuId = registeredMatatuId,
                        operatorId = operatorId,
                        regNumber = regNumber,
                        routeStart = routeStart,
                        routeEnd = routeEnd,
                        stops = stopsCopy,
                        mpesaType = mpesaType,
                        pochiNumber = pochiNumber,
                        paybillNumber = paybillNumber,
                        accountNumber = accountNumber,
                        tillNumber = tillNumber,
                        sendMoneyPhone = sendMoneyPhone,
                        fleetname = selectedFleet?.fleetName ?: "",
                        fleetId = selectedFleet?.fleetId // <-- Pass the selected fleetId
                    )

                    // Log after registration attempt
                    Log.d("MatatuRegistration", "Registration result: $success for ID: $registeredMatatuId, reg: $regNumber")

                    if (success) {
                        // Retrieve the integer matatuId from backend/DB using registration number
                        val savedMatatu = matatuRepository.getMatatuByRegistration(regNumber)
                        val intMatatuId = try { savedMatatu?.matatuId?.toInt() } catch (e: Exception) { null }
                        Log.d("MatatuRegistration", "Verification - Found in database: ${savedMatatu != null}, intMatatuId: $intMatatuId, stringId: ${savedMatatu?.matatuId}")
                        if (intMatatuId != null) {
    Log.d("RegistrationNavigation", "Navigating to FareTabbedActivity with matatuId: $intMatatuId (integer DB ID)")
                            // Navigate to FareTabbedActivity with integer matatuId
                            val context = navController.context
                            val intent = Intent(context, FareTabbedActivity::class.java)
                            intent.putExtra("matatuId", intMatatuId.toString())
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Registration successful but unable to fetch valid matatu ID.", Toast.LENGTH_LONG).show()
                        }

                        // Generate and save QR code
                        try {
                            val multiFormatWriter = MultiFormatWriter()
                            val bitMatrix: BitMatrix = multiFormatWriter.encode(
                                regNumber, // Use registration number for QR code instead of UUID
                                BarcodeFormat.QR_CODE,
                                500,
                                500
                            )
                            val barcodeEncoder = BarcodeEncoder()
                            val qrBitmap = barcodeEncoder.createBitmap(bitMatrix)

                            // Save QR code to gallery
                            saveQRCodeToGallery(qrBitmap, regNumber)

                            // Save QR code to SQLite using QrCodeDao
                            val appDatabase = AppDatabase.getDatabase(context)
                            val qrCodeDao = appDatabase.qrCodeDao()
                            val qrCodeEntity = QrCode(
                                registrationNumber = regNumber,
                                qrImage = bitmapToByteArray(qrBitmap)
                            )
                            qrCodeDao.insertQrCode(qrCodeEntity)
                            Toast.makeText(context, "Registration Successful! QR Code saved locally.", Toast.LENGTH_LONG).show()
                            navController.navigate(Routes.operatorHomeRoute(operatorId)) {
                                popUpTo(Routes.OperatorHome) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Registration successful but failed to generate QR code: ${e.message}", Toast.LENGTH_LONG).show()
                            navController.navigate(Routes.operatorHomeRoute(operatorId)) {
                                popUpTo(Routes.OperatorHome) { inclusive = true }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Registration Failed!", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Text("Register Matatu")
            }

            // Show selected fleet info if any
            selectedFleet?.let { fleet ->
                Text(
                    text = "Selected Fleet: ${fleet.fleetName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = { showFleetDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Fleet")
                }
            }

            // Fleet selection dialog
            if (showFleetDialog) {
                AlertDialog(
                    onDismissRequest = { showFleetDialog = false },
                    title = { Text("Select Fleet") },
                    text = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Column {
                                fleets.forEach { fleet ->
                                    Button(
                                        onClick = {
                                            selectedFleet = fleet
                                            showFleetDialog = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(fleet.fleetName)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedFleet = null
                                        showFleetDialog = false
                                        navController.navigate(Routes.RegistrationScreen.replace("{operatorId}", operatorId))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Register Without Fleet")
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showFleetDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}