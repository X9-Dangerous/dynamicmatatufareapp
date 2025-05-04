package com.example.dynamic_fare.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.dynamic_fare.data.MatatuRepository
import com.example.dynamic_fare.data.FareRepository
import com.example.dynamic_fare.models.MatatuFares
import com.example.dynamic_fare.SetFaresActivity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    Log.d("FareTabbedScreen", "Received matatuId: $matatuId (should be integer DB ID as string)")
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
    Log.d("MatatuDetailsScreen", "Displaying details for matatuId: $matatuId")
    var matatuDetails by remember { mutableStateOf<MatatuDetails?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val matatuRepository = remember(context) { MatatuRepository(context) }

    LaunchedEffect(matatuId) {
        try {
            val matatuIdInt = matatuId.toIntOrNull()
            if (matatuIdInt == null) {
                Toast.makeText(context, "Invalid Matatu ID", Toast.LENGTH_SHORT).show()
                return@LaunchedEffect
            }
            val matatu = matatuRepository.fetchMatatuDetails(matatuIdInt)
            if (matatu != null) {
                matatuDetails = MatatuDetails(
                    matatuId = matatu.matatuId,
                    registrationNumber = matatu.registrationNumber,
                    routeStart = matatu.routeStart,
                    routeEnd = matatu.routeEnd,
                    stops = matatu.stops,
                    mpesaOption = matatu.mpesaOption,
                    pochiNumber = matatu.pochiNumber,
                    sendMoneyPhone = matatu.sendMoneyPhone,
                    tillNumber = matatu.tillNumber,
                    paybillNumber = matatu.paybillNumber,
                    accountNumber = matatu.accountNumber,
                    operatorId = matatu.operatorId
                )
            } else {
                Toast.makeText(context, "Matatu details not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading matatu details: ${e.message}", Toast.LENGTH_SHORT).show()
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
            // QR Code Card
            item {
                var showQrDialog by remember { mutableStateOf(false) }
                var isDownloading by remember { mutableStateOf(false) }
                val context = LocalContext.current
                var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

                // Load QR code from SQLite/Room using registration number
                LaunchedEffect(details.registrationNumber) {
                    val db = com.example.dynamic_fare.AppDatabase.getDatabase(context)
                    val qrCodeDao = db.qrCodeDao()
                    val qrCode = qrCodeDao.getQrCode(details.registrationNumber)
                    if (qrCode != null) {
                        qrBitmap = android.graphics.BitmapFactory.decodeByteArray(qrCode.qrImage, 0, qrCode.qrImage.size)
                    } else {
                        Toast.makeText(context, "Failed to load QR code: No QR code found in local database.", Toast.LENGTH_SHORT).show()
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "QR Code",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                // View QR Code Button
                                IconButton(onClick = { showQrDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = "View QR Code",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                // Download QR Code Button
                                IconButton(
                                    onClick = {
                                        if (qrBitmap != null) {
                                            isDownloading = true
                                            val values = ContentValues().apply {
                                                put(MediaStore.Images.Media.DISPLAY_NAME, "matatu_qr_${details.registrationNumber}.png")
                                                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                                                }
                                            }

                                            try {
                                                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                                                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                                                        qrBitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                                        Toast.makeText(context, "QR Code saved to gallery", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Failed to save QR Code: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isDownloading = false
                                            }
                                        }
                                    },
                                    enabled = qrBitmap != null && !isDownloading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Download QR Code",
                                        tint = if (qrBitmap != null && !isDownloading) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                }
                            }
                        }
                    }
                }

                // QR Code Dialog
                if (showQrDialog && qrBitmap != null) {
                    AlertDialog(
                        onDismissRequest = { showQrDialog = false },
                        title = { Text("QR Code") },
                        text = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier
                                        .size(280.dp)
                                        .padding(16.dp)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showQrDialog = false }) {
                                Text("Close")
                            }
                        }
                    )
                }
            }

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

@Composable
fun DetailRow(
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
    val matatuIdInt = matatuId.toIntOrNull()
    val context = LocalContext.current
    if (matatuIdInt == null) {
        Toast.makeText(context, "Invalid Matatu ID", Toast.LENGTH_SHORT).show()
        return
    }
    Log.d("FareDetailsScreen", "Fetching fares for matatuId: $matatuId (should be integer DB ID)")
    // Declare all state and variables at the top
    var fareData by remember { mutableStateOf<List<MatatuFares>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val fareRepository = remember(context) { FareRepository(context) }
    val activity = context as? ComponentActivity

    // Lifecycle observer for refreshing fares when returning
    DisposableEffect(activity) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    try {
                        val loadedFares = fareRepository.getFaresForMatatu(matatuIdInt!!)
                        fareData = loadedFares
                        Log.d("FareDetailsScreen", "Reloaded fares after SetFaresActivity: $loadedFares for matatu: $matatuIdInt")
                    } catch (e: Exception) {
                        Log.e("FareDetailsScreen", "Error reloading fares: ${e.message}")
                    }
                }
            }
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose {
            activity?.lifecycle?.removeObserver(observer)
        }
    }


    LaunchedEffect(matatuId) {
    coroutineScope.launch {
        val matatuIdInt = matatuId.toIntOrNull()
        if (matatuIdInt == null) {
            Log.e("FareDetailsScreen", "Invalid Matatu ID for fetching fares: $matatuId")
            isLoading = false
            return@launch
        }
        try {
            // Use Room instead of Firebase
            val loadedFares = fareRepository.getFaresForMatatu(matatuIdInt!!)
            fareData = loadedFares
            Log.d("FareDetailsScreen", "Loaded fares: $loadedFares for matatu: $matatuIdInt")
        } catch (e: Exception) {
            Log.e("FareDetailsScreen", "Error loading fares: ${e.message}")
        } finally {
            isLoading = false
        }
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
        } else if (fareData.isEmpty()) {
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
            // Only render fare cards for fares with meaningful data (not just default/empty)
            val realFares = fareData.filter { fare ->
                fare.peakFare > 0.0 || fare.nonPeakFare > 0.0 || fare.rainyPeakFare > 0.0 || fare.rainyNonPeakFare > 0.0 || fare.disabilityDiscount > 0.0
            }
            if (realFares.isEmpty()) {
                // No real fares, show prompt to add fares
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
                realFares.forEach { fare ->
                    // Standard Fares Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (fare.peakFare > 0.0) {
                                    FareRow("Peak Fare", "Ksh ${fare.peakFare}")
                                }
                                if (fare.nonPeakFare > 0.0) {
                                    FareRow("Non-Peak Fare", "Ksh ${fare.nonPeakFare}")
                                }
                                if (fare.rainyPeakFare > 0.0) {
                                    FareRow("Rainy Peak Fare", "Ksh ${fare.rainyPeakFare}")
                                }
                                if (fare.rainyNonPeakFare > 0.0) {
                                    FareRow("Rainy Non-Peak Fare", "Ksh ${fare.rainyNonPeakFare}")
                                }
                                if (fare.disabilityDiscount > 0.0) {
                                    FareRow("Disability Discount", "${fare.disabilityDiscount}%")
                                }
                            }
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

        // Listen for result from SetFaresActivity and refresh fares when returning

    }
}

@Composable
fun FareRow(label: String, value: String) {
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
