package com.example.dynamic_fare

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class BackendFareUpdateService {
    fun updateFare(matatuId: String, oldFare: Int, newFare: Int, route: String) {
        if (oldFare != newFare) { // Only notify if the fare has changed
            saveFareToDatabase(matatuId, newFare)
            sendFareChangeNotification(route, oldFare, newFare)
        }
    }

    private fun sendFareChangeNotification(route: String, oldFare: Int, newFare: Int) {
        val messageData = mapOf(
            "title" to "Matatu Fare Update",
            "body" to "Fare for $route has changed from KES $oldFare to KES $newFare."
        )

        val message = RemoteMessage.Builder("fare_updates@fcm.googleapis.com")
            .setMessageId(System.currentTimeMillis().toString())
            .setData(messageData)
            .build()

        FirebaseMessaging.getInstance().send(message)
    }

    private fun saveFareToDatabase(matatuId: String, newFare: Int) {
        println("✅ Saving new fare for Matatu ID: $matatuId - KES $newFare")
        // TODO: Implement actual database logic (Firebase Firestore / SQL)
    }
}

