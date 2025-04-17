package com.example.dynamic_fare.models

data class Matatu(
val matatuId: String = "",
val operatorId: String = "",
val registrationNumber: String = "",  // Renamed from regNumber
val routeStart: String = "",
val routeEnd: String = "",
val stops: MutableList<String> = mutableListOf(),
val mpesaOption: String = "",  // Renamed from mpesaType
val pochiNumber: String = "",
val paybillNumber: String = "",
val accountNumber: String = "",
val tillNumber: String = "",
val sendMoneyPhone: String = "",
val fleetname: String = ""  // Optional fleetname parameter with empty default
) {

}
