package com.example.dynamic_fare.models

data class MatatuFares(
    val matatuId: String = "",
    val peakFare: Double = 0.0,
    val nonPeakFare: Double = 0.0,
    val rainyPeakFare: Double = 0.0,
    val rainyNonPeakFare: Double = 0.0,
    val disabilityDiscount: Double = 0.0
)
