package com.example.dynamic_fare.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.room.TypeConverter

@Entity(tableName = "matatu")
@TypeConverters(StopsConverter::class)
data class Matatu(
    @PrimaryKey(autoGenerate = false)
    val matatuId: String = "",
    val operatorId: String = "",
    val registrationNumber: String = "",  // Renamed from regNumber
    val routeStart: String = "",
    val routeEnd: String = "",
    val stops: List<String> = listOf(),
    val mpesaOption: String = "",  // Renamed from mpesaType
    val pochiNumber: String = "",
    val paybillNumber: String = "",
    val accountNumber: String = "",
    val tillNumber: String = "",
    val sendMoneyPhone: String = "",
    val fleetname: String = ""  // Optional fleetname parameter with empty default
) {

}

class StopsConverter {
    @TypeConverter
    fun fromStopsList(stops: List<String>): String = stops.joinToString(",")

    @TypeConverter
    fun toStopsList(data: String): List<String> = if (data.isEmpty()) listOf() else data.split(",")
}
