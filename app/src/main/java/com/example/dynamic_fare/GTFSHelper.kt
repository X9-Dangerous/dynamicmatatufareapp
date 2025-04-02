package com.example.dynamic_fare.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object GTFSHelper {
    suspend fun importGTFS(context: Context) {
        withContext(Dispatchers.IO) {  // Run in background thread
            try {
                Log.d("GTFSHelper", "Starting GTFS import")

                val stopsFile = File(context.filesDir, "stops.txt")
                val routesFile = File(context.filesDir, "routes.txt")

                if (stopsFile.exists()) {
                    Log.d("GTFSHelper", "Processing stops.txt")
                }

                if (routesFile.exists()) {
                    Log.d("GTFSHelper", "Processing routes.txt")
                }

                Log.d("GTFSHelper", "GTFS import complete")
            } catch (e: Exception) {
                Log.e("GTFSHelper", "GTFS import failed: ${e.message}", e)
            }
        }
    }
}
