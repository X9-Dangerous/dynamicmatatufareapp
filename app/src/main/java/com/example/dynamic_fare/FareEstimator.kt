package com.example.dynamic_fare

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class EstimatedFare(
    val minFare: Double,
    val maxFare: Double,
    val avgFare: Double,
    val isRainyWeather: Boolean,
    val isPeakHour: Boolean,
    val routeDistance: Double,
    val similarRouteCount: Int
)

class FareEstimator {
    private val database = FirebaseDatabase.getInstance()
    private val faresRef = database.getReference("fares")
    private val DISTANCE_THRESHOLD = 0.5 // 500 meters tolerance for similar routes

    suspend fun estimateFare(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        isRainyWeather: Boolean,
        isPeakHour: Boolean,
        routeDistance: Double
    ): EstimatedFare = suspendCancellableCoroutine { continuation ->
        faresRef.get().addOnSuccessListener { snapshot ->
            try {
                var minFare = Double.MAX_VALUE
                var maxFare = 0.0
                var totalFare = 0.0
                var count = 0

                snapshot.children.forEach { matatuSnapshot ->
                    val routes = matatuSnapshot.child("routes")
                    routes.children.forEach { routeSnapshot ->
                        val routeStartLat = routeSnapshot.child("startLat").getValue(Double::class.java) ?: return@forEach
                        val routeStartLon = routeSnapshot.child("startLon").getValue(Double::class.java) ?: return@forEach
                        val routeEndLat = routeSnapshot.child("endLat").getValue(Double::class.java) ?: return@forEach
                        val routeEndLon = routeSnapshot.child("endLon").getValue(Double::class.java) ?: return@forEach
                        
                        // Check if this is a similar route
                        val startDistance = calculateDistance(startLat, startLon, routeStartLat, routeStartLon)
                        val endDistance = calculateDistance(endLat, endLon, routeEndLat, routeEndLon)
                        
                        if (startDistance <= DISTANCE_THRESHOLD && endDistance <= DISTANCE_THRESHOLD) {
                            // Get fare for this route
                            val baseFare = if (isPeakHour) {
                                if (isRainyWeather) {
                                    matatuSnapshot.child("rainyPeakFare").getValue(Double::class.java) ?: 0.0
                                } else {
                                    matatuSnapshot.child("peakFare").getValue(Double::class.java) ?: 0.0
                                }
                            } else {
                                if (isRainyWeather) {
                                    matatuSnapshot.child("rainyNonPeakFare").getValue(Double::class.java) ?: 0.0
                                } else {
                                    matatuSnapshot.child("nonPeakFare").getValue(Double::class.java) ?: 0.0
                                }
                            }

                            if (baseFare > 0) {
                                minFare = minOf(minFare, baseFare)
                                maxFare = maxOf(maxFare, baseFare)
                                totalFare += baseFare
                                count++
                            }
                        }
                    }
                }

                if (count > 0) {
                    continuation.resume(
                        EstimatedFare(
                            minFare = if (minFare == Double.MAX_VALUE) 0.0 else minFare,
                            maxFare = maxFare,
                            avgFare = totalFare / count,
                            isRainyWeather = isRainyWeather,
                            isPeakHour = isPeakHour,
                            routeDistance = routeDistance,
                            similarRouteCount = count
                        )
                    )
                } else {
                    // If no similar routes found, estimate based on distance
                    val estimatedFare = estimateBasedOnDistance(routeDistance, isPeakHour, isRainyWeather)
                    continuation.resume(
                        EstimatedFare(
                            minFare = estimatedFare * 0.8, // 20% below estimate
                            maxFare = estimatedFare * 1.2, // 20% above estimate
                            avgFare = estimatedFare,
                            isRainyWeather = isRainyWeather,
                            isPeakHour = isPeakHour,
                            routeDistance = routeDistance,
                            similarRouteCount = 0
                        )
                    )
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }
    }

    private fun estimateBasedOnDistance(distance: Double, isPeakHour: Boolean, isRainyWeather: Boolean): Double {
        // Base rate of 20 KSH per kilometer
        val baseFare = distance * 20.0
        
        var finalFare = baseFare
        
        // Add peak hour surge (20% increase)
        if (isPeakHour) {
            finalFare *= 1.2
        }
        
        // Add rainy weather surge (10% increase)
        if (isRainyWeather) {
            finalFare *= 1.1
        }
        
        // Round to nearest 10 shillings
        return (finalFare / 10.0).toInt() * 10.0
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Earth's radius in meters
        val φ1 = lat1 * Math.PI / 180
        val φ2 = lat2 * Math.PI / 180
        val Δφ = (lat2 - lat1) * Math.PI / 180
        val Δλ = (lon2 - lon1) * Math.PI / 180

        val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c // Distance in meters
    }
}
