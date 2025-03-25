package com.example.dynamic_fare

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

object GTFSimporter {
    fun importGTFS(context: Context, database: AppDatabase) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Read directly from asset files instead of zip
                readStopsFromAssets(context, database)
                readRoutesFromAssets(context, database)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun readStopsFromAssets(context: Context, database: AppDatabase) {
        try {
            val stops = mutableListOf<Stop>()
            val inputStream = context.assets.open("stops.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            reader.readLine() // Skip header
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                try {
                    val tokens = line!!.split(",")
                    if (tokens.size >= 4) {
                        stops.add(
                            Stop(
                                stop_id = tokens[0],
                                stop_name = tokens[1],
                                stop_lat = tokens[2].toDoubleOrNull() ?: 0.0,
                                stop_lon = tokens[3].toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip invalid lines
                    e.printStackTrace()
                }
            }
            reader.close()
            
            if (stops.isNotEmpty()) {
                database.stopDao().insertAllStops(stops)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun readRoutesFromAssets(context: Context, database: AppDatabase) {
        try {
            val routes = mutableListOf<Route>()
            val inputStream = context.assets.open("routes.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            reader.readLine() // Skip header
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                try {
                    val tokens = line!!.split(",")
                    if (tokens.size >= 4) {
                        routes.add(
                            Route(
                                route_id = tokens[0],
                                route_short_name = tokens[2], // Adjusted based on your routes.txt format
                                route_long_name = tokens[3].ifEmpty { tokens[2] }, // Use short name if long name is empty
                                route_type = tokens[5].toIntOrNull() ?: 3 // Default to bus (3) if invalid
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip invalid lines
                    e.printStackTrace()
                }
            }
            reader.close()
            
            if (routes.isNotEmpty()) {
                database.routeDao().insertAllRoutes(routes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}