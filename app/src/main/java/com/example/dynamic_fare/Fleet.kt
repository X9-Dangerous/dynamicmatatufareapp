package com.example.dynamic_fare.models

data class Fleet(
    val fleetId: String = "",
    val fleetName: String = "",
    val mpesaNumber: String = "",
    val numberOfCars: Int = 0,
    val routeStart: String = "",
    val routeEnd: String = "",
    val stops: List<String> = emptyList(),
    val operatorId: String = ""
)
