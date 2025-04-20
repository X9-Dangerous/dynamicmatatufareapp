package com.example.dynamic_fare

import androidx.room.*
import com.example.dynamic_fare.models.Fleet

@Dao
interface FleetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFleet(fleet: Fleet)
    
    @Query("SELECT * FROM fleets WHERE fleetId = :fleetId LIMIT 1")
    suspend fun getFleetById(fleetId: String): Fleet?
    
    @Query("SELECT * FROM fleets WHERE operatorId = :operatorId")
    suspend fun getFleetsByOperatorId(operatorId: String): List<Fleet>
    
    @Query("SELECT * FROM fleets")
    suspend fun getAllFleets(): List<Fleet>
    
    @Delete
    suspend fun deleteFleet(fleet: Fleet)
    
    @Query("DELETE FROM fleets WHERE fleetId = :fleetId")
    suspend fun deleteFleetById(fleetId: String)
}
