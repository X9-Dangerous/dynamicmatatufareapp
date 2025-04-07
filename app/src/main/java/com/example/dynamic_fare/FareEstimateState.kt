package com.example.dynamic_fare

sealed class FareEstimateState {
    object Initial : FareEstimateState()
    object Loading : FareEstimateState()
    data class Success(val amount: Double) : FareEstimateState()
    data class Error(val message: String) : FareEstimateState()
}
