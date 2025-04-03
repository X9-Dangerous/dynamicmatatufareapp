package com.example.dynamic_fare

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.*
import okhttp3.*
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.IOException
import android.graphics.Color as AndroidColor
import java.text.DecimalFormat

@Composable
fun MatatuEstimateScreen(navController: NavController = rememberNavController()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var destinationLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var startDestination by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var startingLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var directLine by remember { mutableStateOf<List<GeoPoint>?>(null) }

    // Auto-suggestion states
    var startingSuggestions by remember { mutableStateOf<List<GtfsStopSuggestion>>(emptyList()) }
    var destinationSuggestions by remember { mutableStateOf<List<GtfsStopSuggestion>>(emptyList()) }
    var showStartingSuggestions by remember { mutableStateOf(false) }
    var showDestinationSuggestions by remember { mutableStateOf(false) }

    // New state variables for distance and directions
    var routeDistance by remember { mutableStateOf<Double?>(null) }
    var routeDuration by remember { mutableStateOf<Double?>(null) }
    var routeDirections by remember { mutableStateOf<List<String>>(emptyList()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
        if (isGranted) {
            fetchUserLocation(context, fusedLocationClient) { userLocation = it }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            fetchUserLocation(context, fusedLocationClient) { location ->
                userLocation = location
                // Set the user's location as default starting point
                startingLocation = location
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun fetchRoute(start: GeoPoint, end: GeoPoint) {
        Log.d("ROUTE_FETCH", "Fetching route from ${start.latitude},${start.longitude} to ${end.latitude},${end.longitude}")
        isLoading = true
        errorMessage = null

        // Add direct line first as fallback
        routePoints = listOf(start, end)
        directLine = listOf(start, end)

        // Calculate direct distance as fallback
        val directDistanceInMeters = calculateDistance(start.latitude, start.longitude, end.latitude, end.longitude)
        routeDistance = directDistanceInMeters / 1000 // Convert to kilometers for display
        routeDuration = (directDistanceInMeters / 1000) / 40 * 60 // Rough estimate: 40 km/h average speed, converted to minutes
        routeDirections = listOf("Direct route between points")

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
                    // We still have the direct line as fallback - no need to do anything else
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
                                        if (step.has("maneuver") && step.getJSONObject("maneuver").has("instruction")) {
                                            directions.add(step.getJSONObject("maneuver").getString("instruction"))
                                        } else {
                                            // If no instruction, use type and modifier as fallback
                                            if (step.has("maneuver")) {
                                                val maneuver = step.getJSONObject("maneuver")
                                                val type = if (maneuver.has("type")) maneuver.getString("type") else "continue"
                                                val modifier = if (maneuver.has("modifier")) maneuver.getString("modifier") else ""
                                                directions.add("${type.capitalize()} ${modifier}")
                                            }
                                        }
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
                                    points.add(GeoPoint(point.getDouble(1), point.getDouble(0)))
                                }

                                // Update UI on the main thread
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    routePoints = points
                                    routeDistance = distanceInMeters / 1000 // Convert to kilometers
                                    routeDuration = durationInSeconds / 60 // Convert to minutes
                                    routeDirections = directions
                                    isLoading = false
                                    Log.d("ROUTE_POINTS", "Set ${points.size} route points")
                                    Log.d("ROUTE_INFO", "Distance: ${distanceInMeters/1000} km, Duration: ${durationInSeconds/60} min")
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
                    startingSuggestions = getGtfsStopSuggestions(it, context)
                    showStartingSuggestions = startingSuggestions.isNotEmpty()
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
                    destinationSuggestions = getGtfsStopSuggestions(it, context)
                    showDestinationSuggestions = destinationSuggestions.isNotEmpty()
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
                        fetchCoordinates(startDestination, context) { startLocation ->
                            if (startLocation != null) {
                                Log.d("ROUTE_SEARCH", "Found starting location: ${startLocation.latitude}, ${startLocation.longitude}")
                                startingLocation = startLocation

                                // Get coordinates for destination
                                if (destination.isNotEmpty()) {
                                    fetchCoordinates(destination, context) { destLocation ->
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
                        fetchCoordinates(destination, context) { destLocation ->
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
        var mapView by remember { mutableStateOf<MapView?>(null) }

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
                    if (routePoints.isNotEmpty()) {
                        Log.d("MAP_ROUTE", "Drawing route with ${routePoints.size} points")

                        try {
                            // Create a new polyline
                            val polyline = Polyline()
                            polyline.setPoints(routePoints)
                            polyline.outlinePaint.color = AndroidColor.BLUE
                            polyline.outlinePaint.strokeWidth = 10f

                            // Add it to the map
                            mapView.overlays.add(polyline)

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
                            val simpleLine = Polyline()
                            for (point in directLine!!) {
                                simpleLine.addPoint(point)
                            }
                            simpleLine.outlinePaint.color = AndroidColor.BLUE
                            simpleLine.outlinePaint.strokeWidth = 10f
                            mapView.overlays.add(simpleLine)
                            Log.d("MAP_ROUTE", "Added direct line between points")
                        } catch (e: Exception) {
                            Log.e("MAP_ROUTE", "Failed with direct line approach", e)

                            // Last resort approach
                            if (startingLocation != null && destinationLocation != null) {
                                try {
                                    // Create path with Paint directly
                                    val pathOverlay = object : org.osmdroid.views.overlay.Overlay() {
                                        override fun draw(canvas: android.graphics.Canvas, mapView: MapView, shadow: Boolean) {
                                            val startPoint = mapView.projection.toPixels(startingLocation, null)
                                            val endPoint = mapView.projection.toPixels(destinationLocation, null)

                                            val paint = android.graphics.Paint().apply {
                                                color = AndroidColor.BLUE
                                                strokeWidth = 10f
                                                style = android.graphics.Paint.Style.STROKE
                                            }

                                            val path = android.graphics.Path().apply {
                                                moveTo(startPoint.x.toFloat(), startPoint.y.toFloat())
                                                lineTo(endPoint.x.toFloat(), endPoint.y.toFloat())
                                            }

                                            canvas.drawPath(path, paint)
                                        }
                                    }

                                    mapView.overlays.add(pathOverlay)
                                    Log.d("MAP_ROUTE", "Added custom path overlay")
                                } catch (e: Exception) {
                                    Log.e("MAP_ROUTE", "Failed with all route drawing approaches", e)
                                }
                            }
                        }
                    } else if (startingLocation != null && destinationLocation != null) {
                        // Extra fallback if somehow we have locations but no directLine
                        Log.d("MAP_ROUTE", "Using locations directly for line")
                        try {
                            val simpleLine = Polyline()
                            simpleLine.addPoint(startingLocation)
                            simpleLine.addPoint(destinationLocation)
                            simpleLine.outlinePaint.color = AndroidColor.BLUE
                            simpleLine.outlinePaint.strokeWidth = 10f
                            mapView.overlays.add(simpleLine)
                            Log.d("MAP_ROUTE", "Added direct line using locations")
                        } catch (e: Exception) {
                            Log.e("MAP_ROUTE", "Failed with direct locations line approach", e)
                        }
                    }

                    // Add route information overlay if we have distance data
                    if (routeDistance != null) {
                        try {
                            // Create an info panel overlay
                            val infoOverlay = object : org.osmdroid.views.overlay.Overlay() {
                                override fun draw(canvas: android.graphics.Canvas, mapView: MapView, shadow: Boolean) {
                                    if (shadow) return

                                    // Create paints
                                    val bgPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.argb(220, 255, 255, 255) // Semi-transparent white
                                        style = android.graphics.Paint.Style.FILL
                                    }

                                    val borderPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.BLACK
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = 2f
                                    }

                                    val titlePaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.BLACK
                                        textSize = 32f
                                        isFakeBoldText = true
                                    }

                                    val textPaint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.BLACK
                                        textSize = 26f
                                    }

                                    // Calculate panel size
                                    val width = canvas.width
                                    val height = canvas.height

                                    // Panel will be at bottom left with fixed width
                                    val panelWidth = width * 0.4f
                                    val panelHeight = 100f // Smaller panel without directions

                                    // Position at bottom left with margins
                                    val leftMargin = 20f
                                    val bottomMargin = 20f

                                    // Draw panel background
                                    val rect = android.graphics.RectF(
                                        leftMargin,
                                        height - bottomMargin - panelHeight,
                                        leftMargin + panelWidth,
                                        height - bottomMargin
                                    )
                                    canvas.drawRoundRect(rect, 10f, 10f, bgPaint)
                                    canvas.drawRoundRect(rect, 10f, 10f, borderPaint)

                                    // Format distance and duration
                                    val distanceText = "Distance: ${DecimalFormat("#.##").format(routeDistance)} km"
                                    val timeText = "Time: ${DecimalFormat("#.#").format(routeDuration)} min"

                                    // Draw distance on first line
                                    canvas.drawText(
                                        distanceText,
                                        leftMargin + 10f,
                                        height - bottomMargin - panelHeight + 35f,
                                        textPaint
                                    )

                                    // Draw time on second line
                                    canvas.drawText(
                                        timeText,
                                        leftMargin + 10f,
                                        height - bottomMargin - panelHeight + 70f,
                                        textPaint
                                    )
                                }
                            }

                            // Add the info overlay (above all other overlays)
                            mapView.overlays.add(infoOverlay)
                            Log.d("MAP_INFO", "Added route information overlay to map")
                        } catch (e: Exception) {
                            Log.e("MAP_INFO", "Error adding information overlay", e)
                        }
                    }

                    // If we have route points but no markers set, zoom to show the route
                    if (startingLocation == null && destinationLocation == null && routePoints.size > 1) {
                        try {
                            val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(routePoints)
                            mapView.zoomToBoundingBox(boundingBox, true, 100)
                        } catch (e: Exception) {
                            Log.e("MAP_ERROR", "Error zooming to route bounding box", e)
                        }
                    } else if (directLine != null && directLine!!.size >= 2) {
                        try {
                            val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(directLine!!)
                            mapView.zoomToBoundingBox(boundingBox, true, 100)
                        } catch (e: Exception) {
                            Log.e("MAP_ERROR", "Error zooming to direct line bounding box", e)
                        }
                    }

                    // Force refresh the map
                    mapView.invalidate()
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
            onClick = { /* Handle Payment */ },
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

fun fetchCoordinates(destination: String, context: Context, onResult: (GeoPoint?) -> Unit) {
    Log.d("GEOCODE", "Geocoding address: $destination")
    try {
        val geocoder = android.location.Geocoder(context)
        // Use getFromLocationName in a background thread to avoid NetworkOnMainThreadException
        Thread {
            try {
                val addresses = geocoder.getFromLocationName(destination, 1)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (addresses?.isNotEmpty() == true) {
                        val address = addresses[0]
                        Log.d("GEOCODE", "Found location for '$destination': ${address.latitude}, ${address.longitude}")
                        onResult(GeoPoint(address.latitude, address.longitude))
                    } else {
                        Log.e("GEOCODE", "No location found for '$destination'")
                        onResult(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("GEOCODE", "Error geocoding address: ${e.message}", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onResult(null)
                }
            }
        }.start()
    } catch (e: Exception) {
        Log.e("GEOCODE", "Error initializing geocoder: ${e.message}", e)
        onResult(null)
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

    try {
        // Get all GTFS stops from the stops.txt file
        val gtfsStops = getGtfsStopsFromFile(context)

        // Filter the stops based on user input
        return gtfsStops.filter {
            it.stopName.contains(input, ignoreCase = true) ||
                    it.stopCode.contains(input, ignoreCase = true)
        }.take(15) // Limit results to prevent overwhelming the UI
    } catch (e: Exception) {
        Log.e("GTFS", "Error getting stop suggestions: ${e.message}")
        // Fallback to sample data in case of error
        return getSampleStops().filter {
            it.stopName.contains(input, ignoreCase = true) ||
                    it.stopCode.contains(input, ignoreCase = true)
        }
    }
}

// Helper function to read GTFS stops from the stops.txt file
private fun getGtfsStopsFromFile(context: Context): List<GtfsStopSuggestion> {
    val stops = mutableListOf<GtfsStopSuggestion>()

    try {
        context.assets.open("stops.txt").bufferedReader().use { reader ->
            // Read all lines
            val lines = reader.readLines()

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
                        Log.e("GTFS", "Error parsing stop line: $line", e)
                        // Continue to next line on error
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

// Helper function to calculate distance between two points using Haversine formula
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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

@Preview(showBackground = true)
@Composable
fun MatatuEstimateScreenPreview() {
    MatatuEstimateScreen()
}