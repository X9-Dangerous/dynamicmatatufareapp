package com.example.dynamic_fare

import androidx.room.*
import com.example.dynamic_fare.models.MatatuFares

@Dao
interface FareDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFare(fare: MatatuFares)

    @Query("SELECT * FROM matatu_fares WHERE matatuId = :matatuId LIMIT 1")
    suspend fun getFareByMatatuId(matatuId: String): MatatuFares?

    @Query("SELECT * FROM matatu_fares")
    suspend fun getAllFares(): List<MatatuFares>

    @Delete
    suspend fun deleteFare(fare: MatatuFares)
    
    @Query("DELETE FROM matatu_fares WHERE matatuId = :matatuId")
    suspend fun deleteFareByMatatuId(matatuId: String)
}
