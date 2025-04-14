package com.example.dynamic_fare

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@Composable
fun FareTable(
    distance: Double, // Distance in kilometers
    startPoint: GeoPoint,
    endPoint: GeoPoint,
    onDismiss: () -> Unit,
    fareEstimator: FareEstimator
) {
    Log.d("FARE_DEBUG", "FareTable called with distance: $distance km")
    Log.d("FARE_DEBUG", "Start point: ${startPoint.latitude}, ${startPoint.longitude}")
    Log.d("FARE_DEBUG", "End point: ${endPoint.latitude}, ${endPoint.longitude}")

    var normalFare by remember { mutableStateOf<EstimatedFare?>(null) }
    var peakFare by remember { mutableStateOf<EstimatedFare?>(null) }
    var rainyFare by remember { mutableStateOf<EstimatedFare?>(null) }
    var peakRainyFare by remember { mutableStateOf<EstimatedFare?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(distance) {
        Log.d("FARE_DEBUG", "LaunchedEffect triggered with distance: $distance km")
        isLoading = true
        error = null
        
        try {
            Log.d("FARE_DEBUG", "Starting fare calculations...")
            normalFare = fareEstimator.estimateFare(
                startLat = startPoint.latitude,
                startLon = startPoint.longitude,
                endLat = endPoint.latitude,
                endLon = endPoint.longitude,
                isRainyWeather = false,
                isPeakHour = false,
                routeDistance = distance // Already in kilometers
            )
            Log.d("FARE_DEBUG", "Normal fare calculated: ${normalFare?.avgFare}")

            peakFare = fareEstimator.estimateFare(
                startLat = startPoint.latitude,
                startLon = startPoint.longitude,
                endLat = endPoint.latitude,
                endLon = endPoint.longitude,
                isRainyWeather = false,
                isPeakHour = true,
                routeDistance = distance
            )
            Log.d("FARE_DEBUG", "Peak fare calculated: ${peakFare?.avgFare}")

            rainyFare = fareEstimator.estimateFare(
                startLat = startPoint.latitude,
                startLon = startPoint.longitude,
                endLat = endPoint.latitude,
                endLon = endPoint.longitude,
                isRainyWeather = true,
                isPeakHour = false,
                routeDistance = distance
            )
            Log.d("FARE_DEBUG", "Rainy fare calculated: ${rainyFare?.avgFare}")

            peakRainyFare = fareEstimator.estimateFare(
                startLat = startPoint.latitude,
                startLon = startPoint.longitude,
                endLat = endPoint.latitude,
                endLon = endPoint.longitude,
                isRainyWeather = true,
                isPeakHour = true,
                routeDistance = distance
            )
            Log.d("FARE_DEBUG", "Peak + Rainy fare calculated: ${peakRainyFare?.avgFare}")
            
        } catch (e: Exception) {
            Log.e("FARE_DEBUG", "Error calculating fares: ${e.message}", e)
            error = e.message ?: "Failed to estimate fares"
        } finally {
            isLoading = false
            Log.d("FARE_DEBUG", "Fare calculations completed. isLoading = false")
            Log.d("FARE_DEBUG", "Final fare values: Normal=${normalFare?.avgFare}, Peak=${peakFare?.avgFare}, Rainy=${rainyFare?.avgFare}, PeakRainy=${peakRainyFare?.avgFare}")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Fare Estimates",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Distance: ${String.format("%.1f", distance)} km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                if (normalFare == null && peakFare == null && rainyFare == null && peakRainyFare == null) {
                    Text(
                        text = "No fare estimates available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    FareRow(
                        title = "Normal Hours",
                        description = "Standard fare",
                        fare = normalFare?.avgFare ?: 0.0,
                        minFare = normalFare?.minFare ?: 0.0,
                        maxFare = normalFare?.maxFare ?: 0.0
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    FareRow(
                        title = "Peak Hours",
                        description = "6-9 AM, 4-8 PM",
                        fare = peakFare?.avgFare ?: 0.0,
                        minFare = peakFare?.minFare ?: 0.0,
                        maxFare = peakFare?.maxFare ?: 0.0,
                        icon = Icons.Default.Timeline,
                        iconTint = Color(0xFFE57373)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    FareRow(
                        title = "Rainy Weather",
                        description = "During rainfall",
                        fare = rainyFare?.avgFare ?: 0.0,
                        minFare = rainyFare?.minFare ?: 0.0,
                        maxFare = rainyFare?.maxFare ?: 0.0,
                        icon = Icons.Default.WaterDrop,
                        iconTint = Color(0xFF64B5F6)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    FareRow(
                        title = "Peak + Rain",
                        description = "Peak hours with rain",
                        fare = peakRainyFare?.avgFare ?: 0.0,
                        minFare = peakRainyFare?.minFare ?: 0.0,
                        maxFare = peakRainyFare?.maxFare ?: 0.0,
                        icon = Icons.Default.Timeline,
                        iconTint = Color(0xFFE57373)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun FareRow(
    title: String,
    description: String,
    fare: Double,
    minFare: Double,
    maxFare: Double,
    icon: ImageVector? = null,
    iconTint: Color = LocalContentColor.current
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "KES ${String.format("%.0f", fare)}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Range: ${String.format("%.0f", minFare)} - ${String.format("%.0f", maxFare)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
