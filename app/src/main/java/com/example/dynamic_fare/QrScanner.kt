package com.example.dynamic_fare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size as AndroidSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.dynamic_fare.Routes
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var scannedValue by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasCameraPermission = true
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR Code") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    onBarcodeScanned = { barcodeValue ->
                        if (scannedValue == null) {
                            scannedValue = barcodeValue
                            // Get matatu ID from Firebase using registration number
                            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                            val matatusRef = database.getReference("matatus")
                            
                            android.util.Log.d("QRScanner", "Looking up matatu with registration number: $barcodeValue")
                            
                            // Query all matatus to find matching registration
                            matatusRef.get().addOnSuccessListener { snapshot ->
                                        if (snapshot.exists()) {
                                            android.util.Log.d("QRScanner", "Found matatus in database, searching for registration: $barcodeValue")
                                            var foundMatatu = false
                                            snapshot.children.forEach { matatuSnapshot ->
                                                val registration = matatuSnapshot.child("registrationNumber").value?.toString()
                                                android.util.Log.d("QRScanner", "Checking matatu ${matatuSnapshot.key} with registration: $registration")
                                                if (registration == barcodeValue) {
                                                    foundMatatu = true
                                                    val matatuId = matatuSnapshot.key
                                                    android.util.Log.d("QRScanner", "Found matching matatu! ID: $matatuId")
                                                    navController.navigate(Routes.paymentPageWithQRCode(matatuId!!)) {
                                                        popUpTo(Routes.QRScannerScreen) { inclusive = true }
                                                    }
                                                    return@forEach
                                                }
                                            }
                                            if (!foundMatatu) {
                                                android.util.Log.d("QRScanner", "No matatu found with registration: $barcodeValue")
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "No matatu found with registration number: $barcodeValue",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            android.util.Log.d("QRScanner", "No matatus exist in database")
                                            android.widget.Toast.makeText(
                                                context,
                                                "No matatus found in database",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                            }.addOnFailureListener { e ->
                                android.util.Log.e("QRScanner", "Error querying database: ${e.message}")
                                android.widget.Toast.makeText(
                                    context,
                                    "Error looking up matatu: ${e.message}",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            } else {
                Text(
                    "Camera permission is required",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreview(
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    // Throttle scanning to prevent excessive CPU usage
    var lastProcessingTimeMs by remember { mutableStateOf(0L) }
    val minProcessingIntervalMs = 200L // Don't process more than 5 frames per second
    
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAllPotentialBarcodes()  // Optimize for all possible QR versions
                .build()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            scanner.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = androidx.camera.view.PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(AndroidSize(640, 480))  // Lower resolution is faster
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetRotation(previewView.display.rotation)
                        .setOutputImageRotationEnabled(true)  // Hardware acceleration
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()

                    imageAnalysis.setAnalyzer(executor) { imageProxy ->
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastProcessingTimeMs < minProcessingIntervalMs) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            lastProcessingTimeMs = currentTime
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { value ->
                                            onBarcodeScanned(value)
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
