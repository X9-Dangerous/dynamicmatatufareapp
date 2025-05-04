package com.example.dynamic_fare.api

import com.example.dynamic_fare.models.Fleet
import com.example.dynamic_fare.models.Matatu
import com.example.dynamic_fare.models.MatatuFares
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BackendApiService {
    @POST("/api/matatus/")
    fun createMatatu(@Body matatu: Matatu): Call<Matatu>

    @POST("/api/fleets/")
    fun createFleet(@Body fleet: Fleet): Call<Fleet>

    @POST("/api/fares/")
    fun createFare(@Body fare: MatatuFares): Call<MatatuFares>

    @PUT("/api/fares/{fareId}")
    fun updateFare(@Path("fareId") fareId: Int, @Body fare: MatatuFares): Call<MatatuFares>

    @POST("/api/fares/")
    @Headers("Content-Type: application/json")
    fun createFareRaw(@Body fare: Map<String, @JvmSuppressWildcards Any>): Call<Any>

    @GET("/api/fares/{matatuId}")
    fun getFareByMatatuId(@Path("matatuId") matatuId: Int): Call<MatatuFares>

    @GET("/api/matatus/registration/{regNumber}")
    fun getMatatuByRegistration(@Path("regNumber") regNumber: String): Call<Matatu>

    @GET("/api/matatus/operator/{operatorId}")
    fun getMatatusForOperator(@Path("operatorId") operatorId: String): Call<List<Matatu>>

    @GET("/api/fleets/operator/{operatorId}")
    fun getFleetsForOperator(@Path("operatorId") operatorId: String): Call<List<Fleet>>

    @GET("/api/fares/matatu/{matatuId}")
    fun getFaresForMatatu(@Path("matatuId") matatuId: Int): Call<List<MatatuFares>>

    @GET("/api/matatus/{matatuId}")
    fun getMatatuById(@Path("matatuId") matatuId: Int): Call<Matatu>

    @GET("/api/fleets/{fleetId}")
    fun getFleetById(@Path("fleetId") fleetId: String): Call<Fleet>

    @DELETE("/api/fleets/{fleetId}")
    fun deleteFleet(@Path("fleetId") fleetId: String): Call<Void>

    @DELETE("/api/fares/{matatuId}")
    fun deleteFare(@Path("matatuId") matatuId: Int): Call<Void>

    @GET("/api/matatus/")
    fun readMatatus(): Call<List<Matatu>>

    @DELETE("/api/matatus/{matatuId}")
    fun deleteMatatu(@Path("matatuId") matatuId: String): Call<Void>
}
