package com.example.dynamic_fare.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "fleets")
data class Fleet(
    @PrimaryKey
    @SerializedName("fleetId")
    val fleetId: String = "",
    @SerializedName("name")
    val fleetName: String = "",
    @SerializedName("operatorId")
    val operatorId: String = ""
)
