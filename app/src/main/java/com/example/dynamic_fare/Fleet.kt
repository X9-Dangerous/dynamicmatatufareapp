package com.example.dynamic_fare.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fleets")
data class Fleet(
    @PrimaryKey
    val fleetId: String = "",
    val fleetName: String = "",
    val operatorId: String = ""
)
