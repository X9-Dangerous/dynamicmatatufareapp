package com.example.dynamic_fare

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
    suspend fun estimateFare(
        startLat: Double,
        startLon: Double,
        endLat: Double,
        endLon: Double,
        isRainyWeather: Boolean,
        isPeakHour: Boolean,
        routeDistance: Double
    ): EstimatedFare {
        // Always use fallback estimation based on distance
        val estimatedFare = estimateBasedOnDistance(routeDistance, isPeakHour, isRainyWeather)
        return EstimatedFare(
            minFare = estimatedFare * 0.8, // 20% below estimate
            maxFare = estimatedFare * 1.2, // 20% above estimate
            avgFare = estimatedFare,
            isRainyWeather = isRainyWeather,
            isPeakHour = isPeakHour,
            routeDistance = routeDistance,
            similarRouteCount = 0
        )
    }

    private fun estimateBasedOnDistance(distance: Double, isPeakHour: Boolean, isRainyWeather: Boolean): Double {
        // Base rate of 4.5 KSH per kilometer
        val baseFare = distance * 4.5
        var finalFare = baseFare
        if (isPeakHour) {
            finalFare *= 1.05
        }
        if (isRainyWeather) {
            finalFare *= 1.03
        }
        // Round to nearest 5 shillings
        return (finalFare / 5.0).toInt() * 5.0
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
