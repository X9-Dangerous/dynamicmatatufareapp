package com.example.dynamic_fare.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.room.TypeConverter
import com.google.gson.annotations.SerializedName

@Entity(tableName = "matatu")
@TypeConverters(StopsConverter::class)
data class Matatu(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("matatuId")
    val matatuId: String = "",
    @SerializedName("operatorId")
    val operatorId: String = "",
    @SerializedName("registrationNumber")
    val registrationNumber: String = "",  // Renamed from regNumber
    @SerializedName("routeStart")
    val routeStart: String = "",
    @SerializedName("routeEnd")
    val routeEnd: String = "",
    @SerializedName("stops")
    val stops: List<String> = listOf(),
    @SerializedName("mpesaOption")
    val mpesaOption: String = "",  // Renamed from mpesaType
    @SerializedName("pochiNumber")
    val pochiNumber: String = "",
    @SerializedName("paybillNumber")
    val paybillNumber: String = "",
    @SerializedName("accountNumber")
    val accountNumber: String = "",
    @SerializedName("tillNumber")
    val tillNumber: String = "",
    @SerializedName("sendMoneyPhone")
    val sendMoneyPhone: String = "",
    @SerializedName("fleetname")
    val fleetname: String = "",  // Optional fleetname parameter with empty default
    @SerializedName("fleetId")
    val fleetId: String? = null // Optional fleetId for registration
) {

}

class StopsConverter {
    @TypeConverter
    fun fromStopsList(stops: List<String>): String = stops.joinToString(",")

    @TypeConverter
    fun toStopsList(data: String): List<String> = if (data.isEmpty()) listOf() else data.split(",")
}
