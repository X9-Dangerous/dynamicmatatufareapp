package com.example.dynamic_fare.data

import java.util.Calendar

object TimeRepository {
    fun getCurrentTimeOfDay(onResult: (String) -> Unit) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }
        onResult(timeOfDay)
    }
}
