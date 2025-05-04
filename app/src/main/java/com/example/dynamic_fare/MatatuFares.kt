package com.example.dynamic_fare.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "matatu_fares")
data class MatatuFares(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("fareId")
    val fareId: Int = 0,
    @SerializedName("matatuId")
    val matatuId: Int = 0,
    @SerializedName("peakFare")
    val peakFare: Double = 0.0,
    @SerializedName("nonPeakFare")
    val nonPeakFare: Double = 0.0,
    @SerializedName("rainyPeakFare")
    val rainyPeakFare: Double = 0.0,
    @SerializedName("rainyNonPeakFare")
    val rainyNonPeakFare: Double = 0.0,
    @SerializedName("disabilityDiscount")
    val disabilityDiscount: Double = 0.0
)
