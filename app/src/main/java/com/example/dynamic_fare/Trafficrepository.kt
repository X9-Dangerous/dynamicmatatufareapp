package com.example.dynamic_fare.data

import android.content.Context
import java.io.File
import java.util.zip.ZipFile
import kotlin.math.pow

class GTFSRepository(private val context: Context) {

    fun getDistanceAndTime(startStopId: String, endStopId: String, callback: (Double, Int) -> Unit) {
        val gtfsFile = File(context.filesDir, "gtfs_feed.zip")
        if (!gtfsFile.exists()) {
            callback(0.0, 0) // GTFS data missing, return 0
            return
        }

        val zip = ZipFile(gtfsFile)
        val stops = parseStops(zip)
        val routes = parseRoutes(zip)

        val startStop = stops[startStopId] ?: return callback(0.0, 0)
        val endStop = stops[endStopId] ?: return callback(0.0, 0)

        val distance = calculateGtfsDistance(startStop, endStop)
        val travelTime = getEstimatedTime(routes, startStopId, endStopId)

        callback(distance, travelTime)
    }

    private fun parseStops(zip: ZipFile): Map<String, Stop> {
        val stops = mutableMapOf<String, Stop>()
        zip.getEntry("stops.txt")?.let { entry ->
            zip.getInputStream(entry).bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.split(",")
                    val stopId = parts[0]
                    val lat = parts[2].toDouble()
                    val lon = parts[3].toDouble()
                    stops[stopId] = Stop(stopId, lat, lon)
                }
            }
        }
        return stops
    }

    private fun parseRoutes(zip: ZipFile): Map<String, Route> {
        val routes = mutableMapOf<String, Route>()
        zip.getEntry("routes.txt")?.let { entry ->
            zip.getInputStream(entry).bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val parts = line.split(",")
                    val routeId = parts[0]
                    val routeName = parts[2]
                    routes[routeId] = Route(routeId, routeName)
                }
            }
        }
        return routes
    }

    private fun calculateGtfsDistance(start: Stop, end: Stop): Double {
        val lat1 = Math.toRadians(start.lat)
        val lon1 = Math.toRadians(start.lon)
        val lat2 = Math.toRadians(end.lat)
        val lon2 = Math.toRadians(end.lon)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1
        val a = Math.sin(dLat / 2).pow(2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2).pow(2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371 * c // Earth radius * c = Distance in km
    }

    private fun getEstimatedTime(routes: Map<String, Route>, startStopId: String, endStopId: String): Int {
        // Placeholder for GTFS real-time data processing
        return 20 // Assume 20 minutes for now
    }
}

data class Stop(val id: String, val lat: Double, val lon: Double)
data class Route(val id: String, val name: String)
