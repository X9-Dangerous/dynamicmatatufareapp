package com.example.dynamic_fare

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight as ComposeTextFontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import java.text.DecimalFormat
import java.util.Calendar

// Sealed class for fare estimate state
sealed class FareEstimateState {
    object Initial : FareEstimateState()
    object Loading : FareEstimateState()
    data class Success(
        val baseFare: Double,
        val peakHourFare: Double,
        val rainyDayFare: Double,
        val discountedFare: Double
    ) : FareEstimateState()
    data class Error(val message: String) : FareEstimateState()
}

// Repository for handling fare estimates
class FareEstimateRepository {
    private val firebaseRepo = FareRepository

    suspend fun calculateFareEstimate(
        distance: Double,
        isPeakHour: Boolean,
        isRainyDay: Boolean
    ): FareEstimateState {
        return try {
            // Base rate: 50 KES base + 30 KES per km
            val baseFare = 50.0 + (distance * 30.0)
            
            // Peak hour: 20% increase
            val peakHourFare = if (isPeakHour) baseFare * 1.2 else baseFare
            
            // Rainy day: additional 30% on top of peak/non-peak
            val rainyDayFare = if (isRainyDay) peakHourFare * 1.3 else peakHourFare
            
            // Student discount: 20% off final fare
            val discountedFare = rainyDayFare * 0.8

            FareEstimateState.Success(
                baseFare = baseFare,
                peakHourFare = peakHourFare,
                rainyDayFare = rainyDayFare,
                discountedFare = discountedFare
            )
        } catch (e: Exception) {
            FareEstimateState.Error("Error calculating fare: ${e.message}")
        }
    }
}
import java.io.IOException
import android.graphics.Color as AndroidColor
import java.text.DecimalFormat

@Composable
fun MatatuEstimateScreen(
    navController: NavController = rememberNavController(),
    userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
) {
    Log.d("MatatuEstimateScreen", "Screen loaded with userId: $userId")
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<org.osmdroid.util.GeoPoint?>(null) }
    var destinationLocation by remember { mutableStateOf<org.osmdroid.util.GeoPoint?>(null) }
    var routePoints by remember { mutableStateOf<List<org.osmdroid.util.GeoPoint>>(emptyList()) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startingLocation by remember { mutableStateOf<org.osmdroid.util.GeoPoint?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var directLine by remember { mutableStateOf<List<org.osmdroid.util.GeoPoint>?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Auto-suggestion states
    var startingSuggestions by remember { mutableStateOf<List<GtfsStopSuggestion>>(emptyList()) }
    var destinationSuggestions by remember { mutableStateOf<List<GtfsStopSuggestion>>(emptyList()) }
    var showStartingSuggestions by remember { mutableStateOf(false) }
    var showDestinationSuggestions by remember { mutableStateOf(false) }

    // New state variables for distance and directions
    var routeDistance by remember { mutableStateOf<Double?>(null) }
    var routeDuration by remember { mutableStateOf<Double?>(null) }
    var routeDirections by remember { mutableStateOf<List<String>>(emptyList()) }
    var fareEstimateState by remember { mutableStateOf<FareEstimateState>(FareEstimateState.Initial) }
    val fareEstimateRepository = remember { FareEstimateRepository() }
    val scope = rememberCoroutineScope()
    
    // Preload GTFS data when the screen is first shown
    LaunchedEffect(Unit) {
        try {
            Log.d("GTFS_DEBUG", "Preloading GTFS data from stops1.txt on screen launch")
            val stops = getGtfsStopsFromFile(context)
            Log.d("GTFS_DEBUG", "Preloaded ${stops.size} GTFS stops successfully")
        } catch (e: Exception) {
            Log.e("GTFS_DEBUG", "Error preloading GTFS data: ${e.message}", e)
        }
    }

    // Location permission state
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Location Permission Required") },
            text = { Text("This app needs location permission to find your current location and provide accurate fare estimates. Please grant location permission to continue.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionsLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    // Multiple permissions launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        locationPermissionGranted = fineLocationGranted || coarseLocationGranted
        if (locationPermissionGranted) {
            // Start location updates with high accuracy if fine location is granted
            requestLocationUpdate(context, fusedLocationClient, fineLocationGranted) { location ->
                userLocation = location
                startingLocation = location
                
                // Update map center to user location
                mapView?.let { map ->
                    map.controller?.let { controller ->
                        controller.setCenter(org.osmdroid.util.GeoPoint(location.latitude, location.longitude))
                        controller.setZoom(15.0)
                    }
                }
                
                Toast.makeText(
                    context, 
                    if (fineLocationGranted) "📍 Precise location found" else "📍 Approximate location found", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        when {
            hasFineLocation || hasCoarseLocation -> {
                locationPermissionGranted = true
                requestLocationUpdate(context, fusedLocationClient, hasFineLocation) { location ->
                    userLocation = location
                    startingLocation = location
                    
                    mapView?.controller?.let { controller ->
                        controller.setCenter(org.osmdroid.util.GeoPoint(location.latitude, location.longitude))
                        controller.setZoom(15.0)
                    }
                    
                    Toast.makeText(
                        context, 
                        if (hasFineLocation) "📍 Precise location found" else "📍 Approximate location found", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                // Show permission dialog
                showPermissionDialog = true
            }
        }
            }
            // Request permissions if we don't have them
            else -> {
                // Show permission dialog first
                showPermissionDialog = true
            }
        }
    }

    fun fetchRoute(start: GeoPoint, end: GeoPoint) {
        Log.d("ROUTE_FETCH", "Fetching route from ${start.latitude},${start.longitude} to ${end.latitude},${end.longitude}")
        isLoading = true
        errorMessage = null

        // Set up direct line as fallback
        directLine = listOf(start, end)

        // Note: We're not initializing routePoints here anymore
        // to ensure we don't override the API response points
        // with just start and end

        // Log attempt to fetch route
        Log.d("ROUTE_FETCH_DETAIL", "Attempting to fetch route with ${start.latitude},${start.longitude} -> ${end.latitude},${end.longitude}")

        // Calculate direct distance as fallback
        val directDistanceInMeters = calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
        routeDistance = directDistanceInMeters / 1000 // Convert to kilometers for display
        routeDuration = (directDistanceInMeters / 1000) / 40 * 60 // Rough estimate: 40 km/h average speed, converted to minutes
        routeDirections = listOf("Direct route between points")
        
        // Calculate fare estimate
        fareEstimateState = FareEstimateState.Loading
        scope.launch {
            try {
                val isPeakHour = Calendar.getInstance().let { calendar ->
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val isMondayToFriday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
                    val isMorningPeak = hour in 6..9
                    val isEveningPeak = hour in 16..19
                    isMondayToFriday && (isMorningPeak || isEveningPeak)
                }
                
                fareEstimateRepository.getFareEstimateForRoute(
                    startPoint = start,
                    endPoint = end,
                    distance = routeDistance!!,
                    isPeakHour = isPeakHour
                ).onSuccess { estimate ->
                    fareEstimateState = FareEstimateState.Success(estimate)
                }.onFailure { error ->
                    fareEstimateState = FareEstimateState.Error(error.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("FareEstimate", "Error calculating fare: ${e.message}")
                fareEstimateState = FareEstimateState.Error("Error calculating fare")
            }
        }

        val url = "https://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=geojson&steps=true"
        val client = OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ROUTE_ERROR", "Failed to get route: ${e.message}")
                // Update UI on the main thread
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    isLoading = false
                    errorMessage = "Using direct route (API timeout)"
                    Toast.makeText(context, "Using direct line between points", Toast.LENGTH_SHORT).show()

                    // Set routePoints to directLine as fallback
                    routePoints = directLine ?: listOf(start, end)

                    // No need to force map redraw here, it will redraw on its own in the update function

                    Log.d("ROUTE_FALLBACK", "Using direct route as fallback due to API error")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.body?.let { responseBody ->
                        val responseString = responseBody.string()
                        Log.d("ROUTE_RESPONSE", "Raw response: $responseString")

                        val json = JSONObject(responseString)

                        // Check if routes array exists and has elements
                        if (json.has("routes") && !json.getJSONArray("routes").isNull(0)) {
                            val routes = json.getJSONArray("routes")
                            val firstRoute = routes.getJSONObject(0)

                            // Extract distance and duration
                            val distanceInMeters = firstRoute.getDouble("distance")
                            val durationInSeconds = firstRoute.getDouble("duration")

                            // Extract directions from steps
                            val legs = firstRoute.getJSONArray("legs")
                            val directions = mutableListOf<String>()

                            if (legs.length() > 0) {
                                val firstLeg = legs.getJSONObject(0)
                                if (firstLeg.has("steps")) {
                                    val steps = firstLeg.getJSONArray("steps")
                                    for (i in 0 until steps.length()) {
                                        val step = steps.getJSONObject(i)
                                        val maneuver = step.getJSONObject("maneuver")
                                        val type = if (maneuver.has("type")) maneuver.getString("type") else "continue"
                                        val modifier = if (maneuver.has("modifier")) maneuver.getString("modifier") else ""
                                        val name = if (step.has("name")) step.getString("name") else "the road"
                                        val distance = if (step.has("distance")) step.getDouble("distance") else 0.0
                                        
                                        val instruction = when (type) {
                                            "turn" -> "Turn $modifier onto $name (${String.format("%.0f", distance)}m)"
                                            "depart" -> "Start on $name"
                                            "arrive" -> "🏁 Arrive at destination"
                                            "new name" -> "Continue onto $name (${String.format("%.0f", distance)}m)"
                                            "roundabout" -> "Take the roundabout and exit onto $name"
                                            "merge" -> "Merge ${modifier} onto $name (${String.format("%.0f", distance)}m)"
                                            "fork" -> "Take the $modifier fork onto $name"
                                            "ramp" -> "Take the ramp $modifier onto $name"
                                            else -> "Continue on $name (${String.format("%.0f", distance)}m)"
                                        }
                                        directions.add(instruction)
                                    }
                                }
                            }

                            if (firstRoute.has("geometry") && firstRoute.getJSONObject("geometry").has("coordinates")) {
                                val coordinates = firstRoute.getJSONObject("geometry").getJSONArray("coordinates")
                                Log.d("ROUTE_COORDINATES", "Found ${coordinates.length()} coordinates")

                                val points = mutableListOf<GeoPoint>()
                                for (i in 0 until coordinates.length()) {
                                    val point = coordinates.getJSONArray(i)
                                    // Note: GeoJSON format has [longitude, latitude] order
                                    points.add(org.osmdroid.util.GeoPoint(point.getDouble(1), point.getDouble(0)))
                                }

                                // Update UI on the main thread
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    if (points.size > 2) {
                                        Log.d("ROUTE_POINTS", "Setting ${points.size} route points from API response")
                                        routePoints = points
                                        // No need to force map redraw here, it will redraw on its own in the update function
                                    } else {
                                        Log.w("ROUTE_POINTS", "API returned too few points (${points.size}), using direct line")
                                        // Fall back to direct line if not enough points
                                        routePoints = directLine ?: listOf(start, end)
                                    }

                                    routeDistance = distanceInMeters / 1000 // Convert to kilometers
                                    routeDuration = durationInSeconds / 60 // Convert to minutes
                                    routeDirections = directions
                                    isLoading = false
                                    Log.d("ROUTE_INFO", "Distance: ${distanceInMeters/1000} km, Duration: ${durationInSeconds/60} min")
                                    
                                    // Calculate and zoom to route bounds
                                    val minLat = points.minOf { it.latitude }
                                    val maxLat = points.maxOf { it.latitude }
                                    val minLon = points.minOf { it.longitude }
                                    val maxLon = points.maxOf { it.longitude }
                                    
                                    val bounds = org.osmdroid.util.BoundingBox(
                                        maxLat + 0.001, // Add some padding
                                        maxLon + 0.001,
                                        minLat - 0.001,
                                        minLon - 0.001
                                    )
                                    
                                    // Safely zoom the map
                                    mapView?.let { map ->
                                        map.post {
                                            map.zoomToBoundingBox(bounds, true)
                                        }
                                    }

                                    // Show a success toast
                                    Toast.makeText(context, "Route loaded successfully", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.e("ROUTE_ERROR", "No geometry or coordinates found in route")
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    isLoading = false
                                    errorMessage = "No route found"
                                    Toast.makeText(context, "No route found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Log.e("ROUTE_ERROR", "No routes found in response")
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                isLoading = false
                                errorMessage = "No routes found"
                                Toast.makeText(context, "No routes found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                        Log.e("ROUTE_ERROR", "Empty response body")
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            isLoading = false
                            errorMessage = "Empty response"
                            Toast.makeText(context, "Empty response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ROUTE_ERROR", "Exception parsing response: ${e.message}", e)
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        isLoading = false
                        errorMessage = "Error parsing response: ${e.message}"
                        Toast.makeText(context, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Fare Estimate",
            fontSize = 25.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Start Destination Input with GTFS suggestions
        GtfsInputField(
            value = startDestination,
            onValueChange = {
                startDestination = it
                // Get GTFS suggestions when text changes
                if (it.length >= 2) {
                    Log.d("GTFS_UI", "Getting suggestions for starting point: '$it'")
                    startingSuggestions = getGtfsStopSuggestions(it, context)
                    showStartingSuggestions = startingSuggestions.isNotEmpty()
                    Log.d("GTFS_UI", "Got ${startingSuggestions.size} suggestions, showing=${showStartingSuggestions}")
                } else {
                    startingSuggestions = emptyList()
                    showStartingSuggestions = false
                }
            },
            onSuggestionSelected = { suggestion ->
                startDestination = suggestion.stopName
                startingLocation = GeoPoint(suggestion.latitude, suggestion.longitude)
                showStartingSuggestions = false
            },
            label = "Starting point",
            suggestions = startingSuggestions,
            showSuggestions = showStartingSuggestions,
            onDismissSuggestions = { showStartingSuggestions = false }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Destination Input with GTFS suggestions
        GtfsInputField(
            value = destination,
            onValueChange = {
                destination = it
                // Get GTFS suggestions when text changes
                if (it.length >= 2) {
                    Log.d("GTFS_UI", "Getting suggestions for destination: '$it'")
                    destinationSuggestions = getGtfsStopSuggestions(it, context)
                    showDestinationSuggestions = destinationSuggestions.isNotEmpty()
                    Log.d("GTFS_UI", "Got ${destinationSuggestions.size} suggestions, showing=${showDestinationSuggestions}")
                } else {
                    destinationSuggestions = emptyList()
                    showDestinationSuggestions = false
                }
            },
            onSuggestionSelected = { suggestion ->
                destination = suggestion.stopName
                destinationLocation = GeoPoint(suggestion.latitude, suggestion.longitude)
                showDestinationSuggestions = false
            },
            label = "Destination",
            suggestions = destinationSuggestions,
            showSuggestions = showDestinationSuggestions,
            onDismissSuggestions = { showDestinationSuggestions = false },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                // Perform the same action as the search button
                errorMessage = null
                if (startDestination.isNotEmpty()) {
                    Log.d("ROUTE_SEARCH", "Searching for route from '$startDestination' to '$destination'")

                    // If we have coordinates from suggestions, use them directly
                    if (startingLocation != null && destinationLocation != null) {
                        directLine = listOf(startingLocation!!, destinationLocation!!)
                        fetchRoute(startingLocation!!, destinationLocation!!)
                    } else {
                        // Otherwise use the geocoding process as before
                        // Get coordinates for starting point
                        geocodeAddress(startDestination, context) { startLocation ->
                            if (startLocation != null) {
                                Log.d("ROUTE_SEARCH", "Found starting location: ${startLocation.latitude}, ${startLocation.longitude}")
                                startingLocation = startLocation

                                // Get coordinates for destination
                                if (destination.isNotEmpty()) {
                                    geocodeAddress(destination, context) { destLocation ->
                                        if (destLocation != null) {
                                            Log.d("ROUTE_SEARCH", "Found destination: ${destLocation.latitude}, ${destLocation.longitude}")
                                            destinationLocation = destLocation

                                            // Add direct line as fallback immediately
                                            directLine = listOf(startLocation, destLocation)

                                            // Fetch route between the two points
                                            fetchRoute(startLocation, destLocation)
                                        } else {
                                            Log.e("ROUTE_SEARCH", "Could not find destination location")
                                            Toast.makeText(context, "Could not find destination location", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter a destination", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Log.e("ROUTE_SEARCH", "Could not find starting location")
                                Toast.makeText(context, "Could not find starting location", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else if (userLocation != null && destination.isNotEmpty()) {
                    // If no starting point is entered, use user's current location
                    Log.d("ROUTE_SEARCH", "Using current location as starting point")

                    if (destinationLocation != null) {
                        // Use coordinates from suggestion
                        directLine = listOf(userLocation!!, destinationLocation!!)
                        startingLocation = userLocation
                        fetchRoute(userLocation!!, destinationLocation!!)
                    } else {
                        geocodeAddress(destination, context) { destLocation ->
                            if (destLocation != null) {
                                Log.d("ROUTE_SEARCH", "Found destination: ${destLocation.latitude}, ${destLocation.longitude}")
                                destinationLocation = destLocation
                                startingLocation = userLocation

                                // Add direct line immediately
                                directLine = listOf(userLocation!!, destLocation)

                                fetchRoute(userLocation!!, destLocation)
                            } else {
                                Log.e("ROUTE_SEARCH", "Could not find destination location")
                                Toast.makeText(context, "Could not find destination location", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "Please enter starting point and destination", Toast.LENGTH_SHORT).show()
                }
            })
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Show loading or error message
        if (isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Loading route...", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red, modifier = Modifier.padding(horizontal = 16.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Map

        // Distance and time now appears as an overlay on the bottom left of the map

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(factory = { ctx: Context ->
                    MapView(ctx).apply {
                        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                        controller.setZoom(15.0)
                        val startPoint = GeoPoint(-1.286389, 36.817223) // Nairobi, Kenya
                        controller.setCenter(startPoint)
                        val marker = Marker(this)
                        marker.position = startPoint
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Matatu Stop"
                        overlays.add(marker)
                        mapView = this
                    }
                }, update = { mapView ->
                    // Clear old overlays
                    mapView.overlays.clear()

                    // Add user location marker
                    userLocation?.let { location ->
                        // Center on user location if we have no route or starting location
                        if (startingLocation == null && destinationLocation == null) {
                            mapView.controller.setCenter(location)
                        }

                        val userMarker = Marker(mapView).apply {
                            position = location
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "You are here"
                        }
                        mapView.overlays.add(userMarker)
                    }

                    // If we have a starting location different from user location, show it
                    startingLocation?.let { startLocation ->
                        val startMarker = Marker(mapView).apply {
                            position = startLocation
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Starting Point"
                        }
                        mapView.overlays.add(startMarker)
                    }

                    // Add destination marker if we have one
                    destinationLocation?.let { destination ->
                        val destinationMarker = Marker(mapView).apply {
                            position = destination
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Destination"
                        }
                        mapView.overlays.add(destinationMarker)

                        // If we have both points, zoom to show both
                        startingLocation?.let { startLocation ->
                            try {
                                // Calculate bounds to include both markers
                                val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(listOf(startLocation, destination))
                                // Add some padding
                                val paddedBox = boundingBox.increaseByScale(1.2f)
                                mapView.zoomToBoundingBox(paddedBox, true, 100)
                                Log.d("MAP_ZOOM", "Zoomed to bounding box: $paddedBox")
                            } catch (e: Exception) {
                                Log.e("MAP_ERROR", "Error zooming to points", e)
                                // Fallback: zoom to a more conservative level
                                mapView.controller.setZoom(12.0)
                                // Center on midpoint
                                val midLat = (startLocation.latitude + destination.latitude) / 2
                                val midLon = (startLocation.longitude + destination.longitude) / 2
                                mapView.controller.setCenter(GeoPoint(midLat, midLon))
                                Log.d("MAP_ZOOM", "Used fallback zoom approach")
                            }
                        }
                    }

                    // Draw the route on the map
                    val routePoints = routePoints
                    if (routePoints.isNotEmpty() && routePoints.size > 1) {
                        Log.d("MAP_ROUTE", "Drawing route with ${routePoints.size} points")

                        try {
                            // Create a new polyline
                            val polyline = Polyline()
                            polyline.setPoints(routePoints)
                            Log.d("POLYLINE_DEBUG", "Polyline has ${routePoints.size} points")
                            polyline.outlinePaint.color = AndroidColor.BLUE
                            polyline.outlinePaint.strokeWidth = 10f

                            // Add it to the map
                            mapView.overlays.add(polyline)
                            mapView.invalidate()

                            // Log the successful polyline addition
                            val firstPoint = if (routePoints.isNotEmpty()) routePoints.first() else null
                            val lastPoint = if (routePoints.size > 1) routePoints.last() else null
                            Log.d("POLYLINE_SUCCESS", "Successfully added polyline from ${firstPoint?.latitude},${firstPoint?.longitude} to ${lastPoint?.latitude},${lastPoint?.longitude}")

                            Log.d("MAP_ROUTE", "Added polyline to map overlays")
                        } catch (e: Exception) {
                            Log.e("MAP_ROUTE", "Error drawing polyline", e)

                            // Try a simpler approach if the first one fails
                            try {
                                // Create a simpler polyline with just start and end points if available
                                if (startingLocation != null && destinationLocation != null) {
                                    val simpleLine = Polyline()
                                    simpleLine.addPoint(startingLocation)
                                    simpleLine.addPoint(destinationLocation)
                                    simpleLine.outlinePaint.color = AndroidColor.BLUE
                                    simpleLine.outlinePaint.strokeWidth = 10f
                                    mapView.overlays.add(simpleLine)
                                    Log.d("MAP_ROUTE", "Added simple line between points")
                                }
                            } catch (e2: Exception) {
                                Log.e("MAP_ROUTE", "Failed even with simple line approach", e2)
                            }
                        }
                    }
                    // Use direct line as fallback if we have no route points
                    else if (directLine != null && directLine!!.size >= 2) {
                        Log.d("MAP_ROUTE", "Using direct line as fallback")
                        try {
                            // Draw a simple direct line between start and end
                            val simpleLine = Polyline(mapView)
                            for (point in directLine!!) {
                                simpleLine.addPoint(point)
                            }
                            simpleLine.outlinePaint.color = AndroidColor.BLUE
                            simpleLine.outlinePaint.strokeWidth = 10f
                            mapView.overlays.add(simpleLine)
                            Log.d("MAP_ROUTE", "Added direct line using locations")
                        } catch (e: Exception) {
                            Log.e("MAP_ROUTE", "Failed with direct locations line approach", e)
                        }
                    }

                    // Add distance and time info overlay at bottom left of the map
                    mapView?.let { mapViewInstance ->
                        val distance = routeDistance
                        val duration = routeDuration

                        if (startingLocation != null && destinationLocation != null && distance != null) {
                            try {
                                // Create an overlay for the bottom left corner
                                val infoOverlay = object : org.osmdroid.views.overlay.Overlay() {
                                    override fun draw(canvas: android.graphics.Canvas, mapView: MapView, shadow: Boolean) {
                                        if (shadow) return

                                        try {
                                            // Create background paint
                                            val bgPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.argb(220, 25, 118, 210) // Semi-transparent blue
                                                style = android.graphics.Paint.Style.FILL
                                            }

                                            val borderPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.WHITE
                                                style = android.graphics.Paint.Style.STROKE
                                                strokeWidth = 2f
                                            }

                                            val textPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.WHITE
                                                style = android.graphics.Paint.Style.FILL
                                                textSize = 24f
                                                isFakeBoldText = true
                                            }

                                            val smallTextPaint = android.graphics.Paint().apply {
                                                color = android.graphics.Color.WHITE
                                                style = android.graphics.Paint.Style.FILL
                                                textSize = 20f
                                            }

                                            // Create text with labels
                                            val distanceText = "Distance: ${DecimalFormat("#.#").format(distance)} km"
                                            val timeText = if (duration != null) {
                                                "Estimated Time: ${DecimalFormat("#.#").format(duration)} min"
                                            } else {
                                                ""
                                            }
                                            val fareText = when (fareEstimateState) {
                                                is FareEstimateState.Success -> "Estimated Fare: KES ${(fareEstimateState as FareEstimateState.Success).amount.toInt()}"
                                                is FareEstimateState.Error -> "No Estimate available yet"
                                                FareEstimateState.Loading -> "Calculating..."
                                                FareEstimateState.Initial -> ""
                                            }

                                            // Measure text
                                            val distanceBounds = android.graphics.Rect()
                                            textPaint.getTextBounds(distanceText, 0, distanceText.length, distanceBounds)

                                            val timeBounds = android.graphics.Rect()
                                            if (timeText.isNotEmpty()) {
                                                smallTextPaint.getTextBounds(timeText, 0, timeText.length, timeBounds)
                                            }

                                            // Calculate size and position
                                            val padding = 16f
                                            val margin = 20f
                                            val spacing = 12f

                                            // Measure fare text first
                                            val fareBounds = android.graphics.Rect()
                                            if (fareText.isNotEmpty()) {
                                                textPaint.getTextBounds(fareText, 0, fareText.length, fareBounds)
                                            }

                                            val boxWidth = listOf(
                                                distanceBounds.width(),
                                                timeBounds.width(),
                                                if (fareText.isNotEmpty()) fareBounds.width() else 0
                                            ).maxOrNull()!! + (padding * 2)
                                            
                                            val boxHeight = distanceBounds.height() + 
                                                (if (timeText.isNotEmpty()) timeBounds.height() + spacing else 0f) +
                                                (if (fareText.isNotEmpty()) fareBounds.height() + spacing else 0f) +
                                                (padding * 2)

                                            // Position at bottom left with margin
                                            val rect = android.graphics.RectF(
                                                margin,
                                                mapView.height - boxHeight - margin,
                                                margin + boxWidth,
                                                mapView.height - margin
                                            )

                                            // Draw rounded rectangle
                                            canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
                                            canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

                                            // Draw distance text
                                            canvas.drawText(
                                                distanceText,
                                                rect.left + padding,
                                                rect.top + padding + distanceBounds.height(),
                                                textPaint
                                            )

                                            // Draw time text if available
                                            if (timeText.isNotEmpty()) {
                                                canvas.drawText(
                                                    timeText,
                                                    rect.left + padding,
                                                    rect.top + padding + distanceBounds.height() + spacing + timeBounds.height(),
                                                    smallTextPaint
                                                )
                                            }

                                            // Draw fare text if available
                                            if (fareText.isNotEmpty()) {
                                                val textPaintToUse = when (fareEstimateState) {
                                                    is FareEstimateState.Error -> smallTextPaint.apply { color = android.graphics.Color.RED }
                                                    is FareEstimateState.Success -> textPaint.apply { isFakeBoldText = true }
                                                    else -> textPaint
                                                }
                                                
                                                canvas.drawText(
                                                    fareText,
                                                    rect.left + padding,
                                                    rect.top + padding + distanceBounds.height() + 
                                                        (if (timeText.isNotEmpty()) timeBounds.height() + spacing else 0f) +
                                                        fareBounds.height(),
                                                    textPaintToUse
                                                )
                                            }

                                            // Draw time text if available
                                            if (timeText.isNotEmpty()) {
                                                canvas.drawText(
                                                    timeText,
                                                    rect.left + padding,
                                                    rect.top + padding + distanceBounds.height() + spacing + timeBounds.height(),
                                                    smallTextPaint
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e("MAP_INFO", "Error drawing info overlay: ${e.message}")
                                        }
                                    }
                                }

                                // Add overlay to map
                                mapViewInstance.overlays.add(infoOverlay)
                                Log.d("MAP_INFO", "Added distance/time overlay to bottom left")
                            } catch (e: Exception) {
                                Log.e("MAP_INFO", "Error creating info overlay", e)
                            }
                        }
                    }
                    // Handle map zooming based on available points
                    mapView?.let { mapViewInstance ->
                        try {
                            // Case 1: We have route points but no markers
                            if (startingLocation == null && destinationLocation == null && routePoints.size > 1) {
                                val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(routePoints)
                                mapViewInstance.zoomToBoundingBox(boundingBox, true, 100)
                                Log.d("MAP_ZOOM", "Zoomed to route points")
                            }
                            // Case 2: We have direct line between points
                            else if (directLine != null && directLine!!.size >= 2) {
                                val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(directLine!!)
                                mapViewInstance.zoomToBoundingBox(boundingBox, true, 100)
                                Log.d("MAP_ZOOM", "Zoomed to direct line")
                            }
                            // Case 3: Default case - no zooming needed
                            else {
                                Log.d("MAP_ZOOM", "No points available for zooming")
                            }
                        } catch (e: Exception) {
                            Log.e("MAP_ERROR", "Error zooming map to bounds", e)
                        }
                    }

                    // Force refresh the map
                    mapView?.invalidate()
                }, modifier = Modifier.fillMaxSize())
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Price Estimate Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Price", fontSize = 18.sp, color = Color.Black)
            Text(text = "Ksh", fontSize = 18.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pay Button
        Button(
            onClick = { 
                val auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    navController.navigate(Routes.qrScannerRoute(userId))
                } else {
                    Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text(text = "Pay", fontSize = 18.sp, color = Color.White)
        }

        // Add spacer that pushes the footer to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Use the wider footer component
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            FooterWithIcons(navController)
        }
    }
}

@SuppressLint("MissingPermission")
fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (GeoPoint) -> Unit) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationReceived(GeoPoint(it.latitude, it.longitude))
            }
        }
    }
}

// Geocoding handler class
private fun geocodeAddress(
    address: String,
    context: Context,
    onResult: (org.osmdroid.util.GeoPoint?) -> Unit
) {
    if (address.isBlank()) {
        onResult(null)
        return
    }

    val geocoder = android.location.Geocoder(context)
    val scope = CoroutineScope(Dispatchers.IO + Job())
    
    scope.launch {
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            withContext(Dispatchers.Main) {
                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    val geoPoint = org.osmdroid.util.GeoPoint(location.latitude, location.longitude)
                    onResult(geoPoint)
                } else {
                    onResult(null)
                }
            }
        } catch (e: Exception) {
            Log.e("Geocoding", "Error: ${e.message}")
            withContext(Dispatchers.Main) {
                onResult(null)
            }
        } finally {
            scope.cancel() // Clean up the scope
        }
    }
}

// Data class to hold GTFS stop suggestion
data class GtfsStopSuggestion(
    val stopId: String,
    val stopName: String,
    val stopCode: String,
    val latitude: Double,
    val longitude: Double
)

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
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    } else {
                        IconButton(onClick = { /* Handle Search */ }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                }
            )

            // Show suggestions dropdown
            if (showSuggestions) {
                Log.d("GTFS_UI", "Showing dropdown with ${suggestions.size} suggestions")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .heightIn(max = 200.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(suggestion) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
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

// Function to get GTFS stop suggestions based on input text
fun getGtfsStopSuggestions(input: String, context: Context): List<GtfsStopSuggestion> {
    if (input.isEmpty() || input.length < 2) return emptyList()

    Log.d("GTFS_DEBUG", "Searching for suggestions with input: '$input'")

    try {
        // Get all GTFS stops from the stops.txt file
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

// Helper function to read GTFS stops from the stops.txt file
fun getGtfsStopsFromFile(context: Context): List<GtfsStopSuggestion> {
    val stops = mutableListOf<GtfsStopSuggestion>()

    // Try opening from gtfs subfolder first
    try {
        Log.d("GTFS_DEBUG", "Attempting to open gtfs/stops1.txt")
        context.assets.open("gtfs/stops1.txt").bufferedReader().use { reader ->
            // Read all lines
            val lines = reader.readLines()
            Log.d("GTFS_DEBUG", "Read ${lines.size} lines from stops.txt")

            if (lines.isNotEmpty()) {
                Log.d("GTFS_DEBUG", "Header: ${lines.first()}")
            }

            // Skip header and process all data lines
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
                        Log.e("GTFS_DEBUG", "Error parsing stop line: $line", e)
                        // Continue to next line on error
                    }
                }
            }
        }

        Log.d("GTFS_DEBUG", "Successfully loaded ${stops.size} stops from GTFS data")
        if (stops.size > 0) {
            Log.d("GTFS_DEBUG", "Sample stop: ${stops.first()}")
        }
    } catch (e: Exception) {
        Log.e("GTFS_DEBUG", "Error reading stops.txt file from gtfs folder: ${e.message}", e)
    }

    // If we got no stops, try to read from the root assets folder as a fallback
    if (stops.isEmpty()) {
        try {
            Log.d("GTFS_DEBUG", "Trying to open stops1.txt from root assets folder")
            context.assets.open("stops1.txt").bufferedReader().use { fileReader ->
                // Read all lines
                val lines = fileReader.readLines()
                Log.d("GTFS_DEBUG", "Read ${lines.size} lines from stops.txt in root")

                // Skip header and process all data lines
                if (lines.isNotEmpty()) {
                    lines.drop(1).forEach { fileLine ->
                        val parts = fileLine.split(",")
                        if (parts.size >= 5) {
                            try {
                                val stopId = parts[0].trim()
                                val stopCode = parts[1].trim().ifEmpty { stopId } 
                                val stopName = parts[2].trim()
                                val stopLat = parts[3].trim().toDoubleOrNull() ?: 0.0
                                val stopLon = parts[4].trim().toDoubleOrNull() ?: 0.0

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
                            } catch (e2: Exception) {
                                Log.e("GTFS_DEBUG", "Error parsing stop line from root: $fileLine", e2)
                            }
                        }
                    }
                }

                Log.d("GTFS_DEBUG", "Loaded ${stops.size} stops from root assets folder")
            }
        } catch (e2: Exception) {
            Log.e("GTFS_DEBUG", "Error reading stops.txt from root folder: ${e2.message}", e2)
            Log.e("GTFS_DEBUG", "Stack trace: ${e2.stackTraceToString()}")
        }
    }

    // If we got no stops, try the fallback locations
    if (stops.isEmpty()) {
        Log.d("GTFS_DEBUG", "No stops loaded from file, using sample stops as fallback")
        return getSampleStops()
    }
    
    return stops
}

// Fallback sample stops in case file reading fails
fun getSampleStops(): List<GtfsStopSuggestion> {
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

// Location handling class to avoid type ambiguity
@SuppressLint("MissingPermission")
private fun requestLocationUpdate(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    useHighAccuracy: Boolean,
    onLocationReceived: (org.osmdroid.util.GeoPoint) -> Unit
) {
    try {
        // First try to get last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
            if (lastLocation != null) {
                onLocationReceived(org.osmdroid.util.GeoPoint(lastLocation.latitude, lastLocation.longitude))
            }
        }

        // Then request fresh location updates
        val request = LocationRequest.Builder(2000L).apply {
            setPriority(if (useHighAccuracy) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            setMinUpdateDistanceMeters(5f)
            setMinUpdateIntervalMillis(1000L)
            setMaxUpdateDelayMillis(5000L)
            setWaitForAccurateLocation(useHighAccuracy)
        }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (location.accuracy <= (if (useHighAccuracy) 20f else 50f)) {
                        onLocationReceived(org.osmdroid.util.GeoPoint(location.latitude, location.longitude))
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
    } catch (e: Exception) {
        Log.e("Location", "Error getting location: ${e.message}")
        Toast.makeText(context, "Could not get your location. Please check your GPS settings.", Toast.LENGTH_LONG).show()
    }
}

// Helper function to calculate distance between two points using Haversine formula
public fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371e3 // Earth radius in meters
    val φ1 = lat1 * Math.PI / 180
    val φ2 = lat2 * Math.PI / 180
    val Δφ = (lat2 - lat1) * Math.PI / 180
    val Δλ = (lon2 - lon1) * Math.PI / 180

    val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    return r * c // Distance in meters
}

private fun isCurrentTimePeakHour(): Boolean {
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    
    // Define peak hours (Monday to Friday)
    val isMondayToFriday = dayOfWeek in java.util.Calendar.MONDAY..java.util.Calendar.FRIDAY
    val isMorningPeak = hour in 6..9 // 6 AM to 9 AM
    val isEveningPeak = hour in 16..19 // 4 PM to 7 PM
    
    return isMondayToFriday && (isMorningPeak || isEveningPeak)
}

@Preview(showBackground = true)
@Composable
private fun MatatuEstimateScreenPreview() {
    MatatuEstimateScreen()
}