package com.example.dynamic_fare.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matatu_fares")
data class MatatuFares(
    @PrimaryKey
    val matatuId: String = "",
    val peakFare: Double = 0.0,
    val nonPeakFare: Double = 0.0,
    val rainyPeakFare: Double = 0.0,
    val rainyNonPeakFare: Double = 0.0,
    val disabilityDiscount: Double = 0.0
)
